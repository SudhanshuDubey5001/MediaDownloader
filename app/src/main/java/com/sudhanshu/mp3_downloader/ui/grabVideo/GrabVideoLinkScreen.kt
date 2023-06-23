package com.sudhanshu.mp3_downloader.ui.grabVideo

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sudhanshu.mp3_downloader.R
import com.sudhanshu.mp3_downloader.util.UiEvent
import com.sudhanshu.mp3_downloader.util.Utils.LOG
import com.sudhanshu.mp3_downloader.ui.components.GifImage
import com.sudhanshu.mp3_downloader.ui.components.Header
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/** --------------GRAB VIDEO LINK SCREEN-----------------**/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrabVideoLinkScreen(
    onNavigate: (UiEvent.navigate) -> Unit,
    viewModel: GrabVideoLinkVM = hiltViewModel()
) {

    //collect directory name from VM
    val dirName = viewModel.directoryPath.collectAsState("Select directory")

    // State to enable and disable the textfield
    val enableFieldState = viewModel.enableFieldState.collectAsState(initial = true)

    // State to control the text in YouTube link TextField
    val textState = remember { mutableStateOf(TextFieldValue()) }

    val radiobuttonState = remember { mutableStateOf(true) }

    // to focus/unfocus the keyboard
    val focus = LocalFocusManager.current

    val snackbarHostState = remember { SnackbarHostState() }
    //show gif
    val grabVideoInfoInProgress = remember {
        mutableStateOf(false)
    }

    // For navigation to next screen
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.navigate -> {
                    onNavigate(event)
                }
                UiEvent.popBackStack -> Unit
                is UiEvent.snackbarShow -> {
                    Log.d(LOG, "Showing Snackbar now")
                    val result = snackbarHostState.showSnackbar(
                        message = event.content,
                        actionLabel = event.action,
                        duration = SnackbarDuration.Short
                    )
                    Log.d(LOG, "Snackbar = $result")
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.onGrabVideoLinkEvents(
                            GrabVideoLinkEvents.OnDownloadClick(
                                textState.value.text,
                                radiobuttonState.value
                            )
                        )
                    }
                }
                else -> {}
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        content = {
            Modifier.padding(it)
            MaterialTheme {
                Surface() {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .background(Color.White),
                    ) {
                        //From components directory
                        Header()

                        Text(
                            text = "Media Link",
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.plusjakarta_semibold)),
                                fontSize = 14.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        // YouTube Link
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = Color(0xFFFAFAFA),
                                ),
                            textStyle = TextStyle(
                                fontFamily = FontFamily(Font(R.font.plusjakarta_semibold)),
                            ),
                            shape = RoundedCornerShape(8.dp),
                            value = textState.value,
                            enabled = enableFieldState.value,
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.link_icon),
                                    contentDescription = "",
                                    tint = Color.Unspecified,
                                    modifier = Modifier
                                        .size(30.dp)
                                        .padding(5.dp)
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.clear_icon),
                                    contentDescription = "",
                                    tint = Color.Unspecified,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable {
                                            textState.value = TextFieldValue("")
                                        }
                                )
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            onValueChange = {
                                textState.value = it
                                Log.d("mylog", "Edittext value: " + textState.value)
                            },
                            placeholder = { Text("Paste your link here") },
                            keyboardActions = KeyboardActions(onDone = { focus.clearFocus() },
                                onSearch = {
                                    //implement logic
                                })
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Media format",
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.plusjakarta_semibold)),
                                fontSize = 14.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row {
                            Text(
                                text = "Video",
                                style = TextStyle(
                                    fontFamily = FontFamily(Font(R.font.plusjakarta_semibold)),
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                            )
                            Spacer(modifier = Modifier.width(10.dp))

                            RadioButton(
                                selected = radiobuttonState.value,
                                onClick = { radiobuttonState.value = true },
                                enabled = enableFieldState.value,
                                modifier = Modifier.size(30.dp)
                            )

                            Spacer(modifier = Modifier.width(50.dp))

                            Text(
                                text = "Audio",
                                style = TextStyle(
                                    fontFamily = FontFamily(Font(R.font.plusjakarta_semibold)),
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                            )
                            Spacer(modifier = Modifier.width(10.dp))

                            RadioButton(
                                selected = !radiobuttonState.value,
                                onClick = { radiobuttonState.value = false },
                                enabled = enableFieldState.value,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Destination Folder",
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.plusjakarta_semibold)),
                                fontSize = 14.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // For picking storage location
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = Color(0xFFFAFAFA),
                                ),
                            readOnly = true,
                            enabled = enableFieldState.value,
                            shape = RoundedCornerShape(8.dp),
                            value = dirName.value,
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.storage_icon),
                                    contentDescription = "",
                                    tint = Color.Unspecified,
                                    modifier = Modifier
                                        .size(30.dp)
                                        .padding(5.dp)
                                        .clickable {

                                        }
                                )
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                            onValueChange = {},
                            keyboardActions = KeyboardActions(onDone = { focus.clearFocus() },
                                onSearch = {
                                    //implement logic
                                }),
                            interactionSource = remember { MutableInteractionSource() }.also { mutableInteractionSource ->
                                LaunchedEffect(mutableInteractionSource) {
                                    mutableInteractionSource.interactions.collect {
                                        if (it is PressInteraction.Release) {
                                            Log.d(LOG, "Clicked textfield view")
                                            viewModel.onGrabVideoLinkEvents(GrabVideoLinkEvents.OnDirectoryPickerClicked)
                                        }
                                    }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row {
                            Icon(
                                painter = painterResource(id = R.drawable.info_icon),
                                contentDescription = "info icon",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(7.dp))
                            Text(
                                text = "Where do you want to save the media", style = TextStyle(
                                    fontFamily = FontFamily(Font(R.font.plusjakarta_semibold)),
                                    fontSize = 12.sp,
                                    color = Color(0xFF858181)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Download Button
                        val context = LocalContext.current
                        val scope = rememberCoroutineScope()
                        Button(
                            onClick = {
                                if (textState.value.text == "") {
//                                    Toast.makeText(context, "No URL provided", Toast.LENGTH_SHORT)
//                                        .show()
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "No URL provided",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                } else if (dirName.value == "Select directory") {
//                                    Toast.makeText(
//                                        context,
//                                        "Pick a directory to save the downloaded file",
//                                        Toast.LENGTH_SHORT
//                                    ).show()
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Pick a directory to save the downloaded file",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                } else {
                                    focus.clearFocus()
                                    if (enableFieldState.value) {
                                        grabVideoInfoInProgress.value = true
                                        viewModel.onGrabVideoLinkEvents(
                                            GrabVideoLinkEvents.OnDownloadClick(
                                                textState.value.text,
                                                radiobuttonState.value
                                            )
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(20.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = if (!enableFieldState.value) ButtonDefaults.buttonColors(
                                Color(0xFF9D7CC8)
                            )
                            else ButtonDefaults.buttonColors(Color(0xFF892EFF))
                        ) {
                            Row {
                                if (!enableFieldState.value) GifImage(data = R.drawable.loading)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (!enableFieldState.value) "Grabbing Info..." else "Download",
                                    style = TextStyle(
                                        fontFamily = FontFamily(Font(R.font.plusjakarta_semibold)),
                                        fontSize = 14.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        })
}
