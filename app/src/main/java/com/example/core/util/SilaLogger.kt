package com.example.core.util

import android.util.Log
import com.example.BuildConfig
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Centralized logging utility for Silah that wraps Android Log and Firebase Crashlytics.
 * Suppresses debug and informational developer logs in release builds.
 */
object SilaLogger {

    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }

    fun i(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, message)
        }
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, message, throwable)
        }
        recordNonFatal(throwable)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, message, throwable)
        }
        recordNonFatal(throwable)
    }

    private fun recordNonFatal(throwable: Throwable?) {
        if (throwable == null) return
        try {
            FirebaseCrashlytics.getInstance().recordException(throwable)
        } catch (_: Exception) {
            // Firebase not initialized in local/test environment
        }
    }
}
