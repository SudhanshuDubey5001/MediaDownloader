package com.sudhanshu.mp3_downloader.service

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.sudhanshu.mp3_downloader.util.Utils
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.scopes.ServiceScoped
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking

val onCompleteListener = MutableStateFlow(Utils.DOWNLOADING)

class DownloadCompleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(Utils.LOG, "Intent ---> $intent")
        Log.d(Utils.LOG, "Extras ---> ${intent?.extras}")
        val action = intent?.action
        if (action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
            if (id != -1L) {
                Log.d(Utils.LOG, "Download with DownloadManager Complete!!")
                onCompleteListener.value = Utils.DOWNLOAD_COMPLETE
            } else {
                onCompleteListener.value = Utils.DOWNLOAD_FAILED
            }
        }
    }
}

