package com.storagebundle.core.common.log

import android.util.Log

/**
 * Logging front door for the whole app.
 *
 * Routing every log through one place is what makes the privacy rule in PLAN.md §6
 * enforceable: **file paths, package names, and OCR text must never be logged**, in any
 * build type. Release builds strip these calls via R8; this object exists so that rule has a
 * single place to live rather than being a convention people remember at review time.
 *
 * Pass only bounded, non-identifying values — counts, durations, enum names.
 */
object AppLog {

    /** Whether verbose logging is emitted. R8 folds this to `false` in release builds. */
    private val enabled: Boolean = Log.isLoggable("StorageBundle", Log.DEBUG)

    /** Logs a debug message under [tag]. */
    fun d(tag: String, message: String) {
        if (enabled) {
            Log.d(tag, message)
        }
    }

    /** Logs an informational message under [tag]. */
    fun i(tag: String, message: String) {
        if (enabled) {
            Log.i(tag, message)
        }
    }

    /**
     * Logs a recoverable problem.
     *
     * [throwable] is recorded for its type and stack trace only — never include user content
     * in [message].
     */
    fun w(tag: String, message: String, throwable: Throwable? = null) {
        Log.w(tag, message, throwable)
    }

    /** Logs an unrecoverable problem. Same content restriction as [w]. */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
    }
}
