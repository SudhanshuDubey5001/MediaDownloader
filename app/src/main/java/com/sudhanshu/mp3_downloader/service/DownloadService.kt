package com.sudhanshu.mp3_downloader.service

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.core.net.toUri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sudhanshu.mp3_downloader.util.Utils
import com.sudhanshu.mp3_downloader.util.Utils.LOG
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay

@HiltWorker
class DownloadService @AssistedInject constructor(
    @Assisted val downloadManager: DownloadManager,
    @Assisted val context: Context,
    @Assisted val workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        Log.d(LOG, "Starting service...")
        val metaData = inputData.getString(Utils.METADATA_ID)?.let { Utils.fromJson(it) }
//        val downloadManager = context.getSystemService(DownloadManager::class.java)
        Log.d(LOG, "URL provided ==== ${metaData?.url}")
        val downloadID = downloadMedia(
            downloadManager,
            metaData?.url,
            "video/mp4",
            metaData?.title,
            Utils.TEMP_VIDEO
        )
        var checkDownload = true
        try {
            while (checkDownload) {
                checkDownload = checkDownloadStatus(downloadID)
                delay(1000)
            }
            Log.d(LOG, "Download complete in service!!")
            return Result.success()
        }catch (e: Exception){
            return Result.failure()
        }
    }

    fun downloadMedia(
        downloadManager: DownloadManager,
        url: String?,
        mimetype: String,
        mediaTitle: String?,
        fileName: String
    ): Long {
        val request = DownloadManager.Request(url?.toUri())
            .setMimeType(mimetype)
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle("Downloading")
            .setDescription(mediaTitle)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOCUMENTS, "/$fileName")
        return downloadManager.enqueue(request)
    }

    fun checkDownloadStatus(downloadID: Long): Boolean {
        val query = DownloadManager.Query()
        query.setFilterById(downloadID)
        val cursor = downloadManager.query(query)
        if (cursor.moveToFirst()) {
            val column_status = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            if (column_status > 0) {
                val downloadStatus = cursor.getInt(column_status)
                return downloadStatus != DownloadManager.STATUS_SUCCESSFUL
            }
        }
        return true
    }
}