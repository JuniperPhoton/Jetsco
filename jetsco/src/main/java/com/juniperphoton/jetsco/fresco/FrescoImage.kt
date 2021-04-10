package com.juniperphoton.jetsco.fresco

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.facebook.imagepipeline.request.ImageRequest
import com.juniperphoton.jetsco.imageloading.*

/**
 * @author dengweichao @ Zhihu Inc.
 * @since 2021-04-10
 */
@Composable
fun SimpleLoading(
    backgroundColor: Color = MaterialTheme.colors.surface,
    showProgressBar: Boolean = true,
    progressBarColor: Color = MaterialTheme.colors.primary
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .background(backgroundColor)
            .fillMaxHeight()
            .fillMaxWidth()
    ) {
        if (showProgressBar) {
            CircularProgressIndicator(
                color = progressBarColor,
                modifier = Modifier
                    .width(30.dp)
                    .height(30.dp)
            )
        }
    }
}

@Composable
fun SimpleError(
    backgroundColor: Color = MaterialTheme.colors.surface,
    showRetryButton: Boolean = true,
    onClickRetry: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .background(backgroundColor)
            .fillMaxHeight()
            .fillMaxWidth()
    ) {
        if (showRetryButton) {
            TextButton(onClick = onClickRetry) {
                Text(text = "RETRY")
            }
        }
    }
}

val DefaultLoading: (@Composable BoxScope.() -> Unit) = {
    SimpleLoading()
}

@Composable
fun FrescoImage(
    url: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    fadeAnimation: Boolean = true,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Crop,
    onRequestCompleted: (ImageLoadState) -> Unit = EmptyRequestCompleteLambda,
    onLoading: (@Composable BoxScope.() -> Unit)? = DefaultLoading,
    onError: (@Composable BoxScope.(ImageLoadState.Error) -> Unit)? = null
) {
    val request = ImageRequest.fromUri(url) ?: return

    FrescoImage(
        request = request,
        modifier = modifier,
        onRequestCompleted = onRequestCompleted,
        contentScale = contentScale,
        alignment = alignment,
        fadeAnimation = fadeAnimation,
        onLoading = onLoading,
        onError = onError,
        contentDescription = contentDescription
    )
}

@Composable
fun FrescoImage(
    request: ImageRequest,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    fadeAnimation: Boolean = true,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Crop,
    onRequestCompleted: (ImageLoadState) -> Unit = EmptyRequestCompleteLambda,
    onLoading: (@Composable BoxScope.() -> Unit)? = DefaultLoading,
    onError: (@Composable BoxScope.(ImageLoadState.Error) -> Unit)? = null
) {
    FrescoImage(
        request = request,
        modifier = modifier,
        onRequestCompleted = onRequestCompleted,
    ) { imageState ->
        when (imageState) {
            is ImageLoadState.Success -> {
                MaterialLoadingImage(
                    painter = imageState.painter,
                    contentDescription = contentDescription,
                    fadeInEnabled = fadeAnimation,
                    alignment = alignment,
                    contentScale = contentScale
                )
            }
            is ImageLoadState.Error -> {
                onError?.invoke(this, imageState)
            }
            is ImageLoadState.Loading,
            is ImageLoadState.Empty -> {
                onLoading?.invoke(this)
            }
        }
    }
}

@Composable
fun FrescoImage(
    request: ImageRequest,
    modifier: Modifier = Modifier,
    onRequestCompleted: (ImageLoadState) -> Unit = EmptyRequestCompleteLambda,
    content: @Composable BoxScope.(imageLoadState: ImageLoadState) -> Unit
) {
    ImageLoad(
        request = request,
        executeRequest = { r: ImageRequest ->
            loadImage(r)
        },
        transformRequestForSize = { r, _ ->
            r
        },
        requestKey = request,
        onRequestCompleted = onRequestCompleted,
        modifier = modifier,
        content = content,
    )
}

private suspend fun loadImage(request: ImageRequest): ImageLoadState {
    return try {
        val dataSource = when {
            ImageIO.isInBitmapCache(request) -> {
                DataSource.MEMORY
            }
            ImageIO.isInDiskCache(request) -> {
                DataSource.DISK
            }
            else -> DataSource.NETWORK
        }
        val drawable = ImageIO.fetchDrawable(request)
        ImageLoadState.Success(drawable.toPainter(), dataSource)
    } catch (e: ImageIO.FetchException) {
        ImageLoadState.Error(throwable = e)
    }
}