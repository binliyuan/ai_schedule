package com.solunis.schedule.data.ai

import android.util.Log

object AiLogger {

    var debugEnabled = true

    private const val PREFIX = "AI"

    fun i(tag: String, msg: String) {
        if (debugEnabled) Log.i("$PREFIX/$tag", msg)
    }

    fun d(tag: String, msg: String) {
        if (debugEnabled) Log.d("$PREFIX/$tag", msg)
    }

    fun w(tag: String, msg: String) {
        Log.w("$PREFIX/$tag", msg)
    }

    fun e(tag: String, msg: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e("$PREFIX/$tag", msg, throwable)
        } else {
            Log.e("$PREFIX/$tag", msg)
        }
    }

    fun section(tag: String, title: String) {
        if (debugEnabled) Log.i("$PREFIX/$tag", "========== $title ==========")
    }

    fun divider(tag: String, label: String) {
        if (debugEnabled) Log.i("$PREFIX/$tag", "--- $label ---")
    }

    fun truncate(text: String, max: Int = 300): String {
        return if (text.length > max) text.take(max) + "...(${text.length} chars)" else text
    }
}
