package com.sudhanshu.mp3_downloader.ui.download

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.arthenica.mobileffmpeg.FFmpeg
import com.sudhanshu.mp3_downloader.service.DownloadService
import com.sudhanshu.mp3_downloader.service.onCompleteListener
import com.sudhanshu.mp3_downloader.util.Utils
import com.sudhanshu.mp3_downloader.util.Utils.LOG
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.*
import java.time.Duration
import javax.inject.Inject

/**-----------DOWNLOAD VIEW MODEL CLASS------------**/
/** In this screen we perform 3 things :
 * 1. Download the Youtube URL video file
 * 2. Convert the video file into mp3
 * 3. Copy converted file from app internal directory to user specified directory **/

@HiltViewModel
class DownloadVM @Inject constructor(
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    //watch the download and other progresses
    private val _progressSlider = MutableStateFlow(0f)
    val progressSlider = _progressSlider.asStateFlow()

    //determine which stage we are in
    private val _operationStage = MutableSharedFlow<String>()
    val operationStage = _operationStage.asSharedFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    // get the MetaData object to populate the video info
    private val metadataJSON = savedStateHandle.get<String>(Utils.METADATA_ID)
    val metadata = metadataJSON?.let { Utils.fromJson(it) }

    private val tempStorage =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath

    init {
        startDownloadWithDownloadManager()
    }

    fun startDownloadWithDownloadManager() {
        val constraints =
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val inoutData = Data.Builder()
            .putString(Utils.METADATA_ID, metadataJSON)
            .build()
        val workerRequest = OneTimeWorkRequestBuilder<DownloadService>()
            .setConstraints(constraints)
            .setInputData(inoutData)
//            .setInitialDelay(Duration.ofSeconds(10))  //to delay the service
            .setBackoffCriteria(    // to set the policy for Result.Retry()
                backoffPolicy = BackoffPolicy.LINEAR,   // means it will retry in 15 seconds and for exponential = 15, 30, 60...
                duration = Duration.ofSeconds(15)   // duration
            )
            .build()

        viewModelScope.launch(Dispatchers.IO) {
            _operationStage.emit(Utils.DOWNLOADING)
            WorkManager.getInstance(context).enqueue(workerRequest)
            onCompleteListener.collect {
                Log.d(LOG, "Listener ran!!!!! ----> $it")
                when (it) {
                    Utils.DOWNLOAD_COMPLETE -> {
                        onCompleteListener.value = Utils.DOWNLOADING    //reset the listener
                        if (metadata?.mediaFormat == true) {
                            moveFileToDestination(tempStorage + "/${Utils.TEMP_VIDEO}")
                        } else {
                            convertToMp3(
                                tempStorage + "/${Utils.TEMP_VIDEO}",
                                tempStorage + "/${Utils.TEMP_AUDIO}"
                            )
                        }
                    }
                    Utils.DOWNLOAD_FAILED -> {
                        _operationStage.emit(Utils.FAILED)
                        freeUpResources()
                    }
                }
            }
        }
    }

    private fun convertToMp3(inputFilePath: String, outputFilePath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _operationStage.emit(Utils.CONVERTING)
            _progressSlider.value = -1f
            val command = arrayOf(
                "-i",
                inputFilePath,
                "-vn",
                "-c:a",
                "libmp3lame",
                "-qscale:a",
                "0",            //-qscale:a is set to 0 to get the highest quality of mp3
                outputFilePath
            )
            try {
                val s = FFmpeg.execute(command)
                Log.d(LOG, "success = $s")
                moveFileToDestination(outputFilePath)
            } catch (e: IOException) {
                updateOperationStage(Utils.FAILED)
                updateErrorMessage(e.message.toString())
            }
        }
    }

    private fun moveFileToDestination(sourceFilePath: String) {
        updateOperationStage(Utils.SAVING)
        try {
            val sourceFile = File(sourceFilePath)
            var destinationFile: File

            //get the title name after removing specialCharacters, emojis, symbols and glyphs
            val mediaFileName = metadata?.title?.let { Utils.removeSpecialCharactersAndEmojis(it) }
            Log.d(LOG, "Name of file : $mediaFileName")

            // setup the final file extension
            val fileExtension =
                if (metadata?.mediaFormat == true)
                    "mp4"
                // else it is audio in mp3
                else "mp3"

            val destinationFullPath = metadata?.destinationPath + "/$mediaFileName.$fileExtension"

            //create a new file at destination path with the file name
            destinationFile = File(destinationFullPath)

            //add suffix if the file name exists
            var i = 1;
            while (destinationFile.exists()) {
                destinationFile =
                    File(metadata?.destinationPath + "/$mediaFileName($i).$fileExtension")
                i++
            }
            if (sourceFile.exists()) {
                val inputStream = FileInputStream(sourceFile)
                val outputStream = FileOutputStream(destinationFile)

                val bufferSize = 8 * 1024 // 8 KB buffer size
                val buffer = ByteArray(bufferSize)
                var bytesRead: Int
                var totalBytesRead: Long = 0
                val fileSize = sourceFile.length()

                while (inputStream.read(buffer).also { bytesRead = it } > 0) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead

                    // progress of file transfer
                    _progressSlider.value = totalBytesRead.toFloat() / fileSize * 100
                }

                inputStream.close()
                outputStream.close()
                updateOperationStage(Utils.SUCCESS)
                Log.d(LOG, "File moved successfully to external storage.")
            } else {
                Log.d(LOG, "Source file not found.")
            }
        } catch (e: IOException) {
            // Handle any IO exceptions
            e.printStackTrace()
        }


        //delete all the temporary files
        freeUpResources()
    }

    fun freeUpResources() {
        val tempVideoFile = File(tempStorage + "/${Utils.TEMP_VIDEO}")
        val tempAudioFile = File(tempStorage + "/${Utils.TEMP_AUDIO}")
        if (tempVideoFile.exists()) {
            tempVideoFile.delete()
        }
        if (tempAudioFile.exists()) {
            tempAudioFile.delete()
        }
    }

    fun updateOperationStage(stage: String) {
        viewModelScope.launch {
            _operationStage.emit(stage)
        }
    }

    fun updateErrorMessage(message: String) {
        viewModelScope.launch {
            _errorMessage.emit(message)
        }
    }
}
