package com.nicknam.vshareit_for_reddit.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.nicknam.vshareit_for_reddit.CheckHttpConnectionAsyncTask
import com.nicknam.vshareit_for_reddit.R
import com.nicknam.vshareit_for_reddit.VRedditConvertService

fun share(context: Context, url: String) {
    val downloadUri = generateDownloadUri(url)
    if (!validateDownloadUri(downloadUri))
        Toast.makeText(context, R.string.toast_invalid_url, Toast.LENGTH_SHORT).show()
    else {
        CheckHttpConnectionAsyncTask {
            when (it) {
                200 -> {
                    Toast.makeText(context, R.string.toast_fetching_video, Toast.LENGTH_SHORT).show()
                    startConversion(context, downloadUri)
                }
                403 -> Toast.makeText(context, R.string.toast_video_not_found, Toast.LENGTH_SHORT).show()
                else -> Toast.makeText(context, R.string.toast_server_error, Toast.LENGTH_SHORT).show()
            }
        }.execute(downloadUri.toString())
    }
}

fun startConversion(context: Context, downloadUri: Uri) {
    context.startService(Intent(context, VRedditConvertService::class.java).apply {
        putExtra(Intent.EXTRA_STREAM, downloadUri)
    })
}