package com.juniperphoton.jetsco

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.facebook.imagepipeline.request.ImageRequest
import com.juniperphoton.jetsco.fresco.DefaultLoading
import com.juniperphoton.jetsco.fresco.FrescoImage
import com.juniperphoton.jetsco.fresco.SimpleLoading
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

private val list = listOf(
    "https://pic1.zhimg.com/v2-50f586f4ff0a1425b854fa027bfcb091_r.webp",
    "https://pic4.zhimg.com/50/v2-ac8ff9b6f79ec97987f082b4a8a4b4c6_r.webp",
    "https://pic2.zhimg.com/v2-776b468a4cac3befb026aaff5ab5ea5f_720w.jpg",
    "https://pic4.zhimg.com/50/v2-b428a95239375a232e7a0fde0aa70f86_r.jpg",
    "https://pic4.zhimg.com/v2-de1a46f298c9d23804208558007ef4b7_r.gif"
)

@Composable
fun MainContent() {
    Column(modifier = Modifier.padding(12.dp)) {
        Text(text = "Hello Jetsco!")

        Spacer(modifier = Modifier.height(12.dp))

        ImageList(list)
    }
}

@Composable
fun ImageList(images: List<String>) {
    LazyColumn {
        items(images) { url ->
            Image(url)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun Image(url: String) {
    val request = ImageRequest.fromUri(url) ?: return

    FrescoImage(
        request = request,
        modifier = Modifier
            .height(300.dp)
            .fillMaxWidth(),
        onLoading = {
            SimpleLoading(showProgressBar = false)
        },
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    JetscoTheme {
        MainContent()
    }
}