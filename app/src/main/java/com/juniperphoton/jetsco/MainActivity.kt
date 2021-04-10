package com.juniperphoton.jetsco

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.facebook.imagepipeline.request.ImageRequest
import com.juniperphoton.jetsco.fresco.FrescoImage
import com.juniperphoton.jetsco.fresco.SimpleError
import com.juniperphoton.jetsco.ui.theme.JetscoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JetscoTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    MainContent()
                }
            }
        }
    }
}

private val url =
    "https://juniperphoton.dev/myersplash/wallpapers/20210221.jpg"

private val errorUrl =
    "https://juniperphoton.dev/myersplash/wallpapers/2021022122.jpg"

@Composable
fun MainContent() {
    Column(modifier = Modifier.padding(12.dp)) {
        Text(text = "Hello Jetsco!")

        Spacer(modifier = Modifier.height(50.dp))

        Image()
    }
}

@Composable
fun Image() {
    val retry = remember {
        mutableStateOf(false)
    }

    val request = when (retry.value) {
        false -> {
            remember {
                ImageRequest.fromUri(Uri.parse(errorUrl))
            }
        }
        true -> {
            remember {
                ImageRequest.fromUri(Uri.parse(url))
            }
        }
    }

    request?.also {
        FrescoImage(
            request = it,
            modifier = Modifier
                .height(300.dp),
            onError = {
                SimpleError {
                    if (!retry.value) {
                        retry.value = true
                    }
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    JetscoTheme {
        MainContent()
    }
}