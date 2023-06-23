package com.sudhanshu.mp3_downloader.ui.download

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sudhanshu.mp3_downloader.R
import com.sudhanshu.mp3_downloader.util.Utils
import com.sudhanshu.mp3_downloader.ui.components.Header

/**---------------------DOWNLOAD SCREEN UI------------------------**/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadAudioScreen(
    popBackStack: () -> Unit,
    viewModel: DownloadVM = hiltViewModel()
) {
    val progressSlider = viewModel.progressSlider.collectAsState()
    val operationStage = viewModel.operationStage.collectAsState(initial = Utils.DOWNLOADING)
    val errorMessage = viewModel.errorMessage.collectAsState(initial = "Error occurred")

    val showButtonAndMessage =
        operationStage.value == Utils.SUCCESS || operationStage.value == Utils.FAILED

    LaunchedEffect(Unit) {
        // TODO: popbackstack
    }

    MaterialTheme {
        Surface {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .background(Color.White)
                    .verticalScroll(rememberScrollState()),
            ) {
                Header()

                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFfafafa))
                    ) {
                        Column {
                            Card(
                                modifier = Modifier
                                    .padding(16.dp),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                AsyncImage(
                                    model = viewModel.metadata?.thumbnail,
                                    contentDescription = "cover_picture",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                )
                            }

                            viewModel.metadata?.title?.let {
                                Text(
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 0.dp
                                    ),
                                    text = it,
                                    style = TextStyle(
                                        fontFamily = FontFamily(Font(R.font.plusjakarta_semibold)),
                                        fontSize = 14.sp
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.padding(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 0.dp,
                                    bottom = 16.dp
                                )
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.views_icon),
                                    contentDescription = "views icon",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(7.dp))
                                Text(
                                    text = Utils.convertNumberTo_KMB_format(
                                        viewModel.metadata?.views
                                            ?: "Unspecified"
                                    ) + " Views",
                                    style = TextStyle(
                                        fontFamily = FontFamily(Font(R.font.plusjakarta_semibold)),
                                        fontSize = 12.sp,
                                        color = Color(0xFF858181)
                                    )
                                )

                                Spacer(modifier = Modifier.width(32.dp))

                                Icon(
                                    painter = painterResource(id = R.drawable.thumbs_up),
                                    contentDescription = "likes icon",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(7.dp))
                                Text(
                                    text = Utils.convertNumberTo_KMB_format(
                                        viewModel.metadata?.likes
                                            ?: "Unspecified"
                                    ) + " Likes",
                                    style = TextStyle(
                                        fontFamily = FontFamily(Font(R.font.plusjakarta_semibold)),
                                        fontSize = 12.sp,
                                        color = Color(0xFF858181)
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(modifier = Modifier.padding(16.dp)) {
                        Row() {
                            Text(
                                modifier = Modifier.weight(1f),
                                text = operationStage.value,
                                style = TextStyle(
                                    fontFamily = FontFamily(Font(R.font.plusjakarta_semibold)),
                                    fontSize = 12.sp
                                ),
                            )
                            Text(
                                text = if (progressSlider.value > 0) progressSlider.value.toString() + "%" else "",
                                style = TextStyle(
                                    fontFamily = FontFamily(Font(R.font.plusjakarta_semibold)),
                                    fontSize = 12.sp,
                                    color = Color(0xFF858181)
                                ),
                            )
                        }

                        DownloadAndSavingProgress(progressSlider.value, operationStage.value)

                        if (showButtonAndMessage) {
                            Row() {
                                Icon(
                                    painter = if (operationStage.value == Utils.SUCCESS)
                                        painterResource(id = R.drawable.success_icon)
                                    else painterResource(id = R.drawable.info_icon),
                                    contentDescription = "result icon",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(20.dp)
                                )

                                Spacer(modifier = Modifier.width(7.dp))

                                Text(
                                    text = if (operationStage.value == Utils.SUCCESS)
                                        "Media successfully saved into selected folder"
                                    else "Please check your internet connection",
                                    style = TextStyle(
                                        fontFamily = FontFamily(Font(R.font.plusjakarta_semibold)),
                                        fontSize = 12.sp,
                                        color = Color(0xFF858181)
                                    )
                                )
                            }
                        }
                    }

                    if (showButtonAndMessage) {
                        Column {
                            Spacer(modifier = Modifier.weight(1f))
                            Button(
                                onClick = { popBackStack() },   //to go back to main screen
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        top = 16.dp,
                                        bottom = 20.dp,
                                        start = 16.dp,
                                        end = 16.dp
                                    ),
                                contentPadding = PaddingValues(20.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(Color(0xFF892EFF)),
                            ) {
                                Text(
                                    text = "Download Another Media",
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
        }
    }
}

@Composable
fun DownloadAndSavingProgress(progress: Float, stage: String) {
    when (stage) {
        Utils.DOWNLOADING -> {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .padding(horizontal = 0.dp, vertical = 16.dp),
                color = Color(0xFF3690FA)
            )
        }
        Utils.CONVERTING -> {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .padding(horizontal = 0.dp, vertical = 16.dp),
                color = Color(0xFFFFBB0E)
            )
        }
        Utils.SAVING -> {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .padding(horizontal = 0.dp, vertical = 16.dp),
                color = Color(0xFF28C6D0)
            )
        }
        Utils.SUCCESS, Utils.FAILED -> {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .padding(horizontal = 0.dp, vertical = 16.dp),
                progress = 1f,
                color = if (stage == Utils.SUCCESS) Color(0xFF72BD6C) else Color(0xFFDC4A4A)
            )
        }
    }
}