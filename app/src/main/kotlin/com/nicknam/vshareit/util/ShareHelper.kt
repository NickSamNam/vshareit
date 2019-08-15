package com.nicknam.vshareit.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.widget.Toast
import com.nicknam.vshareit.*
import com.nicknam.vshareit.ConversionResultReceiver.Companion.RESULT_CODE_FETCHED_FROM_CACHE
import com.nicknam.vshareit.ConversionResultReceiver.Companion.RESULT_CODE_FETCHED_FROM_SOURCE
import com.nicknam.vshareit.VRedditConvertService.Companion.EXTRA_RESULT_RECEIVER

fun share(context: Context, url: String) {
    GenerateDownloadUriTask {uri ->
        if (uri == null) {
            Toast.makeText(context, R.string.toast_invalid_url, Toast.LENGTH_SHORT).show()
            return@GenerateDownloadUriTask
        }

        CheckHttpConnectionTask { responseCode ->
            when (responseCode) {
                200, 100 -> {
                    Toast.makeText(context, R.string.toast_fetching_video, Toast.LENGTH_SHORT).show()
                    val resultReceiver = ConversionResultReceiver(Handler()).apply {
                        subscribe(object : ConversionResultReceiver.Receiver {
                            override fun onCompletion(resultCode: Int, contentUri: Uri?) {
                                when (resultCode) {
                                    RESULT_CODE_FETCHED_FROM_SOURCE, RESULT_CODE_FETCHED_FROM_CACHE -> {
                                        val shareIntent: Intent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_STREAM, contentUri)
                                            type = "video/mp4"
                                        }
                                        (context as Activity).startActivity(
                                            Intent.createChooser(
                                                shareIntent,
                                                context.resources.getText(R.string.share_label)
                                            ).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            })
                                    }
                                    else -> {
                                        Toast.makeText(context, R.string.toast_ffmpeg_error, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        })
                    }
                    context.startService(Intent(context, VRedditConvertService::class.java).apply {
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(EXTRA_RESULT_RECEIVER, resultReceiver)
                    })
                }
                403, 404 -> Toast.makeText(context, R.string.toast_video_not_found, Toast.LENGTH_SHORT).show()
                -2 -> Toast.makeText(context, R.string.toast_network_error, Toast.LENGTH_SHORT).show()
                else -> Toast.makeText(context, R.string.toast_server_error, Toast.LENGTH_SHORT).show()
            }
        }.execute(uri.toString())
    }.execute(url)
}