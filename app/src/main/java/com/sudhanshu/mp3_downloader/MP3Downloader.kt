package com.sudhanshu.mp3_downloader

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.sudhanshu.mp3_downloader.service.DownloadService
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MP3Downloader : Application(), Configuration.Provider{

    @Inject
    lateinit var downloadServiceFactory: DownloadServiceFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setWorkerFactory(downloadServiceFactory)
            .build()
}

class DownloadServiceFactory @Inject constructor(
    private val downloadManager: DownloadManager
): WorkerFactory(){
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker = DownloadService(downloadManager,appContext,workerParameters)
}