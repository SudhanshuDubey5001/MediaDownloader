package com.sudhanshu.mp3_downloader.ui.grabVideo

/** ----SEALED CLASS FOR HANDLING ANY EVENTS RELATED TO GRABVIDEOLINKSCREEN EVENTS TRIGGERED BY VIEW---**/

sealed class GrabVideoLinkEvents {

    data class OnDownloadClick(val youtubeURL: String, val mediaFormat: Boolean) : GrabVideoLinkEvents()

    object OnDirectoryPickerClicked : GrabVideoLinkEvents()
}