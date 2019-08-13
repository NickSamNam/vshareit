package com.nicknam.vshareit_for_reddit

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver

class ConversionResultReceiver(handler: Handler?) : ResultReceiver(handler) {

    private val receivers = mutableListOf<Receiver>()

    interface Receiver {
        fun onCompletion(resultCode: Int, contentUri: Uri?)
    }

    fun subscribe(receiver: Receiver) {
        receivers += receiver
    }

    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
        receivers.forEach { it.onCompletion(resultCode, resultData?.getParcelable<Uri>(KEY_CONTENT_URI)) }
    }

    companion object {
        const val KEY_CONTENT_URI = "CONTENT_URI"

        const val RESULT_CODE_STORAGE_ERROR = -1
        const val RESULT_CODE_OPERATION_CANCELLED = -2
        const val RESULT_CODE_FFMPEG_ERROR = -3
        const val RESULT_CODE_FETCHED_FROM_CACHE = 1
        const val RESULT_CODE_FETCHED_FROM_SOURCE = 2
    }
}