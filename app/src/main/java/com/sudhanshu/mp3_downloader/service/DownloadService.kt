package com.sudhanshu.mp3_downloader.service

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.sudhanshu.mp3_downloader.util.Utils
import com.sudhanshu.mp3_downloader.util.Utils.LOG

class DownloadService(
    val context: Context,
    val workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        Log.d(LOG, "Starting service...")
        val metaData = inputData.getString(Utils.METADATA_ID)?.let { Utils.fromJson(it) }
        val downloadManager = context.getSystemService(DownloadManager::class.java)
        Log.d(LOG, "URL provided ==== ${metaData?.url}")
        val result = downloadMedia(
            downloadManager,
            metaData?.url,
            "video/mp4",
            metaData?.title,
            Utils.TEMP_VIDEO
        )
        Log.d(LOG, "Result of download from service ==== ${result}")
        return Result.success()
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
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOCUMENTS,"/$fileName")
        return downloadManager.enqueue(request)
    }
}