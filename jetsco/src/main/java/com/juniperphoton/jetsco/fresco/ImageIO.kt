package com.juniperphoton.jetsco.fresco

import android.graphics.Bitmap
import com.facebook.common.executors.CallerThreadExecutor
import com.facebook.common.references.CloseableReference
import com.facebook.datasource.DataSource
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber
import com.facebook.imagepipeline.image.CloseableImage
import com.facebook.imagepipeline.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * @author dengweichao @ Zhihu Inc.
 * @since 2021-04-10
 */
internal object ImageIO {
    private val pipeline = Fresco.getImagePipelineFactory().imagePipeline

    class FetchException(message: String) : IllegalStateException(message)

    suspend fun isInDiskCache(url: String): Boolean {
        return withContext(Dispatchers.IO) {
            convertUrlToRequest(url) {
                pipeline.isInDiskCacheSync(this)
            }
        }
    }

    suspend fun isInBitmapCache(url: String): Boolean {
        return withContext(Dispatchers.IO) {
            convertUrlToRequest(url) {
                pipeline.isInBitmapMemoryCache(this)
            }
        }
    }

    suspend fun isInDiskCache(imageRequest: ImageRequest): Boolean {
        return withContext(Dispatchers.IO) {
            pipeline.isInDiskCacheSync(imageRequest)
        }
    }

    suspend fun isInBitmapCache(imageRequest: ImageRequest): Boolean {
        return withContext(Dispatchers.IO) {
            pipeline.isInBitmapMemoryCache(imageRequest)
        }
    }

    @Throws(FetchException::class)
    suspend fun fetchBitmap(url: String): Bitmap {
        return convertUrlToRequest(url) {
            fetchBitmap(this)
        }
    }

    @Throws(FetchException::class)
    suspend fun fetchBitmap(imageRequest: ImageRequest): Bitmap {
        return suspendCancellableCoroutine { c ->
            val dataSource = pipeline.fetchDecodedImage(imageRequest, null)

            c.invokeOnCancellation {
                dataSource.close()
            }

            dataSource.subscribe(object : BaseBitmapDataSubscriber() {
                override fun onNewResultImpl(bitmap: Bitmap?) {
                    if (c.isCompleted) {
                        return
                    }

                    val bm = bitmap ?: kotlin.run {
                        onFailure()
                        return
                    }

                    c.resume(bm)
                }

                override fun onFailureImpl(dataSource: DataSource<CloseableReference<CloseableImage>>) {
                    if (c.isCompleted) {
                        return
                    }

                    c.resumeWithException(FetchException("Cannot fetch bitmap"))
                }

                private fun onFailure() {
                    c.resumeWithException(FetchException("Cannot fetch bitmap"))
                }
            }, CallerThreadExecutor.getInstance())
        }
    }

    @Throws(IllegalArgumentException::class)
    private suspend fun <R> convertUrlToRequest(
        url: String,
        block: (suspend ImageRequest.() -> R)
    ): R {
        val request =
            ImageRequest.fromUri(url) ?: throw IllegalArgumentException("url is not valid")
        return request.block()
    }
}