package com.nicknam.vshareit_for_reddit

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver

class ConversionResultReceiver(handler: Handler?) : ResultReceiver(handler) {

    private val receivers = mutableListOf<Receiver>()

    interface Receiver {
        fun onCompletion(returnCode: Int, commandOutput: String, contentUri: Uri?)
    }

    fun subscribe(receiver: Receiver) {
        receivers += receiver
    }

    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
        receivers.forEach { it.onCompletion(resultCode, resultData?.getString(KEY_COMMAND_OUTPUT) ?: "", resultData?.getParcelable<Uri>(KEY_CONTENT_URI)) }
    }

    companion object {
        const val KEY_COMMAND_OUTPUT = "COMMAND_OUTPUT"
        const val KEY_CONTENT_URI = "CONTENT_URI"
    }
}