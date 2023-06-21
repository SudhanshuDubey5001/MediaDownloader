package com.sudhanshu.mp3_downloader.data

data class MetaData(
    val title: String?,
    val views: String?,
    val likes: String?,
    val thumbnail: String?,
    val url: String,
    val destinationPath: String,
    val mediaFormat: Boolean
)
