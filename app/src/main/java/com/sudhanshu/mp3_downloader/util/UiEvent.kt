package com.sudhanshu.mp3_downloader.util

/**------Handle UI events-------**/

sealed class UiEvent {

    object popBackStack: UiEvent()

    data class navigate(val route: String): UiEvent()

    data class snackbarShow(
        val content: String,
        val action: String? = null
    ) : UiEvent()
}