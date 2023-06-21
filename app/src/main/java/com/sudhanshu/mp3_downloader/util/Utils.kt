package com.sudhanshu.mp3_downloader.util

import android.content.Intent
import android.provider.DocumentsContract
import android.util.Log
import androidx.activity.result.ActivityResult
import com.google.gson.Gson
import com.sudhanshu.mp3_downloader.data.MetaData
import java.io.File
import java.text.Normalizer

/** ---------------UTIL CLASS FOR FLEXIBLE FUNCTIONS---------------------**/

object Utils {

    const val LOG = "mylog"

    const val DOWNLOADING = "Downloading..."
    const val CONVERTING = "Converting..."
    const val SAVING = "Saving..."
    const val SUCCESS = "Success"
    const val FAILED = "Failed"

    const val TEMP_AUDIO = "temporaryAudioFile.mp3"
    const val TEMP_VIDEO = "temporaryVideoFile.mp4"

    const val DOWNLOAD_COMPLETE = "download_complete"
    const val DOWNLOAD_FAILED = "download_failed"

    //to pass around data from one screen to other----->
    const val METADATA_ID = "video_metadata_id"
    fun toJson(metadata: MetaData) = Gson().toJson(metadata)
    fun fromJson(json: String): MetaData = Gson().fromJson(json, MetaData::class.java)

    //converter to 100K, 1B etc
    fun convertNumberTo_KMB_format(number: String): String {
        val suffixes = arrayOf("", "K", "M", "B")

        var value = number.toDoubleOrNull() ?: return number
        var suffixIndex = 0

        while (value >= 1000 && suffixIndex < suffixes.size - 1) {
            value /= 1000
            suffixIndex++
        }

        return "%.1f%s".format(value, suffixes[suffixIndex])
    }

    //Get proper file path
    fun getDirectoryShortPath(result: ActivityResult): String{
        val data: Intent? = result.data
        Log.d(LOG, "result = ${data.toString()}")
        val uri = data?.data
        val docUri = DocumentsContract.buildDocumentUriUsingTree(
            uri,
            DocumentsContract.getTreeDocumentId(uri)
        )
        Log.d(LOG,"docURI---> ${docUri.path}")

        return docUri.lastPathSegment.toString().substringAfter(":")
    }

    //Get the filename with extension
    fun getTemporaryFileName(tempFileRef: File):String{
        val listFiles = tempFileRef.listFiles()
        if (listFiles != null) {
            for (file in listFiles) {
                if (file.isFile && file.name.contains("temporaryVideoFile")) {
                    Log.d(LOG, "Found it = ${file.name}")
                    return file.name
                }else{
                    Log.d(LOG, "File not created")
                }
            }
        }
        return "-1"
    }

    fun deleteAllFiles(dirPath: File){
        val listFiles = dirPath.listFiles()
        if (listFiles != null) {
            for (file in listFiles) {
                file.delete()
            }
        }
    }

    //Get the normalize String from the title of video
    fun removeSpecialCharactersAndEmojis(input: String): String {
        val symbolRegex = "[^A-Za-z0-9 ]".toRegex()
        val emojiRegex = "[^\\p{L}\\p{N}\\p{P}\\p{Z}]".toRegex()

        val removedSymbols = input.replace(symbolRegex, "")
        return removedSymbols.replace(emojiRegex, "")
    }
}