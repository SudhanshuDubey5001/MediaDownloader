package com.sudhanshu.mp3_downloader.ui.grabVideo

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Environment
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.gson.Gson
import com.sudhanshu.mp3_downloader.data.MetaData
import com.sudhanshu.mp3_downloader.data.PythonObject
import com.sudhanshu.mp3_downloader.retrievedFileDirectoryRef
import com.sudhanshu.mp3_downloader.util.Routes
import com.sudhanshu.mp3_downloader.util.UiEvent
import com.sudhanshu.mp3_downloader.util.Utils
import com.sudhanshu.mp3_downloader.util.Utils.LOG
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**------------------GRAB VIDEO LINK VIEW MODEL---------------------**/
/** Here we retrieve the youtube URL, final directory for saving the audio mp3 from the user **/
/** Then we retrieve the Youtube video metadata and send it to the Download screen **/

// shared flow to open file explorer from Mainactivity
val fileExplorer = MutableSharedFlow<Boolean>()

// shared flow to display messages using Mainactivity
val toastMsg = MutableStateFlow<String>("Welcome")

@HiltViewModel
class GrabVideoLinkVM @Inject constructor(
//    private val youtubeInstance: YoutubeDL,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // to handle the overall UI events
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    // to set the short directory path in the View
    private val _directoryPath = MutableSharedFlow<String>()
    val directoryPath = _directoryPath.asSharedFlow()

    private val _enableFieldState = MutableStateFlow(true)
    val enableFieldState = _enableFieldState.asStateFlow()

    // to set the full directory path for file writing purpose
    private var selectedDirPath: String = ""

    init {
        //retrieve the directory picked by user
        viewModelScope.launch(Dispatchers.IO) {
            retrievedFileDirectoryRef.collectLatest {
                //store the file path to send it to Download screen
                selectedDirPath = Environment.getExternalStorageDirectory().absolutePath + "/" + it
                Log.d(LOG, "Selected path ====== $selectedDirPath")

                //send the name to update View
                _directoryPath.emit(it)
            }
        }
    }

    fun onGrabVideoLinkEvents(events: GrabVideoLinkEvents) {
        when (events) {
            is GrabVideoLinkEvents.OnDownloadClick -> {
                grabYoutubeVideoMetadata(events.youtubeURL, events.mediaFormat)
            }
            GrabVideoLinkEvents.OnDirectoryPickerClicked -> {
                viewModelScope.launch {
                    fileExplorer.emit(true)
                }
            }
        }
    }

    private fun grabYoutubeVideoMetadata(url: String, format: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (isOnline()) {
                _enableFieldState.value = false
                try {
                    //get video info
//                    val videoInfo = youtubeInstance.getInfo(url)

                    //set the default directory to Music if user did not pick
                    val defaultDirectory =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).absolutePath

                    if (!Python.isStarted()) {
                        Python.start(AndroidPlatform(context))
                    }

                    val python = Python.getInstance()
                    val videoInfoPythonRef =
                        python.getModule("video_metadata")  // Update module name

                    val result = videoInfoPythonRef.callAttr("get_info", url)
                    Log.d(LOG, "Result === ${result}")
                    val videoInfo = Gson().fromJson(result.toString(), PythonObject::class.java)

                    val metadata = MetaData(
                        title = videoInfo.title,
                        views = videoInfo.views.toString(),
                        likes = videoInfo.likes.toString(),
                        thumbnail = videoInfo.thumbnail,
                        url = videoInfo.url,
                        destinationPath = if (selectedDirPath != "") selectedDirPath else defaultDirectory,
                        mediaFormat = format
                    )

                    //go to next screen with metadata of video in JSON format
                    _uiEvent.emit(
                        UiEvent.navigate(
                            Routes.DOWNLOAD_SCREEN + "?metadata=" + Utils.toJson(metadata)
                        )
                    )
                    _enableFieldState.value = true
                } catch (e: Exception) {
                    Log.d(LOG, "Exception: ${e.message}")
                    _enableFieldState.value = true   //release all the views
                    _uiEvent.emit(
                        UiEvent.snackbarShow(
                            "Please provide a valid URL or the URL is restricted",
                        )
                    )
                }
            } else {
                _uiEvent.emit(
                    UiEvent.snackbarShow(
                        "Check your internet connection",
                        "Retry"
                    )
                )
            }
        }
    }

    //check if the user has internet connection
    fun isOnline(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val activeNetwork =
            connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}