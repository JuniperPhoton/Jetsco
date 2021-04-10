package com.juniperphoton.jetsco.fresco

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.facebook.common.executors.CallerThreadExecutor
import com.facebook.common.references.CloseableReference
import com.facebook.datasource.DataSource
import com.facebook.datasource.DataSubscriber
import com.facebook.drawee.backends.pipeline.DefaultDrawableFactory
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber
import com.facebook.imagepipeline.image.CloseableImage
import com.facebook.imagepipeline.request.ImageRequest
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * @author dengweichao @ Zhihu Inc.
 * @since 2021-04-10
 */
@SuppressLint("StaticFieldLeak")
internal object ImageIO {
    private val pipeline = Fresco.getImagePipelineFactory().imagePipeline

    private val context = Fresco.getImagePipeline().config.context.applicationContext

    private val drawableFactory = DefaultDrawableFactory(
        context.resources,
        Fresco.getImagePipelineFactory().getAnimatedDrawableFactory(context)
    )

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
    suspend fun fetchDrawable(url: String): Drawable {
        return convertUrlToRequest(url) {
            fetchDrawable(this)
        }
    }

    @Throws(FetchException::class)
    suspend fun fetchDrawable(imageRequest: ImageRequest): Drawable {
        return suspendCancellableCoroutine { c ->
            val dataSource = pipeline.fetchDecodedImage(imageRequest, null)

            c.invokeOnCancellation {
                dataSource.close()
            }

            dataSource.subscribe(object : DataSubscriber<CloseableReference<CloseableImage>> {
                override fun onNewResult(dataSource: DataSource<CloseableReference<CloseableImage>>) {
                    val image = dataSource.result?.get() ?: kotlin.run {
                        c.failed()
                        return
                    }

                    val drawable = drawableFactory.createDrawable(image) ?: kotlin.run {
                        c.failed()
                        return
                    }

                    c.resume(drawable)
                }

                override fun onFailure(dataSource: DataSource<CloseableReference<CloseableImage>>) {
                    c.failed()
                }

                override fun onCancellation(dataSource: DataSource<CloseableReference<CloseableImage>>) {
                    c.failed()
                }

                override fun onProgressUpdate(dataSource: DataSource<CloseableReference<CloseableImage>>) {
                    // ignored
                }
            }, CallerThreadExecutor.getInstance())
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

                    c.failed()
                }

                private fun onFailure() {
                    c.failed()
                }
            }, CallerThreadExecutor.getInstance())
        }
    }

    private fun <T> CancellableContinuation<T>.failed() {
        this.resumeWithException(FetchException("Cannot fetch bitmap"))
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