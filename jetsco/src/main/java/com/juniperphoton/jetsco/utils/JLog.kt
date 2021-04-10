package com.juniperphoton.jetsco.utils

import android.util.Log
import com.juniperphoton.jetsco.BuildConfig

/**
 * @author dengweichao @ Zhihu Inc.
 * @since 2021-04-10
 */
object JLog {
    inline fun debug(tag: String = "JLog", message: () -> String) = runOnDebug {
        Log.d(tag, message())
    }

    inline fun info(tag: String = "JLog", message: () -> String) = runOnDebug {
        Log.i(tag, message())
    }

    inline fun warn(tag: String = "JLog", message: () -> String) = runOnDebug {
        Log.w(tag, message())
    }

    inline fun error(tag: String = "JLog", message: () -> String) = runOnDebug {
        Log.e(tag, message())
    }

    inline fun runOnDebug(block: () -> Unit) {
        if (BuildConfig.DEBUG) {
            block()
        }
    }
}