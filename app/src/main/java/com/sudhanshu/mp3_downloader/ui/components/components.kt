package com.sudhanshu.mp3_downloader.ui.components

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.size.Size
import com.sudhanshu.mp3_downloader.R

/**--------COMMON UI COMPONENTS-------------**/

@Composable
fun Header() {
    Row(
        modifier = Modifier.padding(
            start = 0.dp,
            top = 48.dp,
            end = 0.dp,
            bottom = 30.dp
        )
    ) {
        Icon(
            painter = painterResource(id = R.drawable.yt_icon),
            contentDescription = "YouTube Icon",
            modifier = Modifier.size(34.dp),
            tint = Color.Unspecified
        )
        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "Media Downloader",
            style = TextStyle(
                fontFamily = FontFamily(Font(R.font.plusjakarta_semibold)),
                fontSize = 20.sp
            )
        )
    }
}

@Composable
fun GifImage(data: Any?) {
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components {
            if (Build.VERSION.SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()
    Image(
        painter = rememberAsyncImagePainter(
            ImageRequest.Builder(context).data(data = data).apply(
                block = { size(Size.ORIGINAL) }
            )
                .build(),
            imageLoader = imageLoader,
        ),
        contentDescription = null,
    )
}