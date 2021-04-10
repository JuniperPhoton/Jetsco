package com.juniperphoton.jetsco

import android.app.Application
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * @author dengweichao @ Zhihu Inc.
 * @since 2021-04-10
 */
class App : Application() {
    companion object {
        lateinit var instance: Application
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        val client = OkHttpClient.Builder()
            .connectTimeout(15_000, TimeUnit.MILLISECONDS)
            .readTimeout(15_000, TimeUnit.MILLISECONDS)
            .writeTimeout(15_000, TimeUnit.MILLISECONDS)
            .build()
        val config = OkHttpImagePipelineConfigFactory
            .newBuilder(this, client)
            .build()
        Fresco.initialize(this, config)
        Fresco.getImagePipeline().clearCaches()
    }
}