package com.sudhanshu.mp3_downloader

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sudhanshu.mp3_downloader.ui.download.DownloadAudioScreen
import com.sudhanshu.mp3_downloader.ui.grabVideo.GrabVideoLinkScreen
import com.sudhanshu.mp3_downloader.ui.grabVideo.fileExplorer
import com.sudhanshu.mp3_downloader.ui.grabVideo.toastMsg
import com.sudhanshu.mp3_downloader.ui.theme.MP3_DownloaderTheme
import com.sudhanshu.mp3_downloader.util.Routes
import com.sudhanshu.mp3_downloader.util.Utils
import com.sudhanshu.mp3_downloader.util.Utils.LOG
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

//shared flow to initialize user picked directory path
val retrievedFileDirectoryRef = MutableSharedFlow<String>()

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    lateinit var launcher: ActivityResultLauncher<Intent>
    lateinit var requestPermissionlauncher: ActivityResultLauncher<String>

    init {
        //setup the launcher for file explorer to open
        launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                Log.d(LOG, "Result code = ${result.resultCode}")
                if (result.resultCode == Activity.RESULT_OK) {
                    //get directory path
                    val shortPathOfSelectedDir = Utils.getDirectoryShortPath(result)
                    //emit the directory path to viewModel
                    lifecycleScope.launch(Dispatchers.IO) {
                        retrievedFileDirectoryRef.emit(shortPathOfSelectedDir)
                    }
                } else {
                    Toast.makeText(this@MainActivity, "No directory selected", Toast.LENGTH_SHORT)
                        .show()
                }
            }

        //setup the Permission launcher
        requestPermissionlauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    isPermissionGranted()
                } else {
                    Toast.makeText(this@MainActivity, "Not enough permissions", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    override fun onDestroy() {
        launcher.unregister()
        requestPermissionlauncher.unregister()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //observe the sharedFlow reference from ViewModel in order to open file explorer
        lifecycleScope.launch {
            fileExplorer.collectLatest {
                if (it) {
                    //ask for permission
                    isPermissionGranted()
                }
            }
            //state to show toast messages coming from ViewModel
            Toast.makeText(this@MainActivity, toastMsg.value, Toast.LENGTH_SHORT).show()
        }

        //Compose navigation
        setContent {
            MP3_DownloaderTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = Routes.GRABVIDEO_SCREEN  //initial screen
                ) {
                    composable(Routes.GRABVIDEO_SCREEN) {
                        GrabVideoLinkScreen(
                            onNavigate = {
                                navController.navigate(it.route)    //to go to DownloadAudioScreen
                            }
                        )
                    }
                    composable(
                        route = Routes.DOWNLOAD_SCREEN + "?metadata={${Utils.METADATA_ID}}",
                        arguments = listOf(
                            navArgument(name = Utils.METADATA_ID) {
                                type = NavType.StringType
                                defaultValue = "-1"
                            }
                        )
                    ) {
                        DownloadAudioScreen(
                            popBackStack = {
                                navController.popBackStack()    //to pop back to initial screen
                            }
                        )
                    }
                }
            }
        }
    }

    fun openFileExplorer() {
        Log.d(LOG, "Initialing file picker")
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        launcher.launch(intent)
    }

    private fun isPermissionGranted() {
        val readPermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        val writePermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                openFileExplorer()
            } else { //request for the permission
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
        } else {
            if (readPermission == PackageManager.PERMISSION_GRANTED) {
                if (writePermission == PackageManager.PERMISSION_GRANTED) {
                    openFileExplorer()
                } else {
                    requestPermissionlauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            } else {
                requestPermissionlauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }
}