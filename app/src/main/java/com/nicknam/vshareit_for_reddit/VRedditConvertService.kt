package com.nicknam.vshareit_for_reddit

import android.app.IntentService
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.util.Log
import androidx.core.content.FileProvider
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.arthenica.mobileffmpeg.FFmpeg.RETURN_CODE_CANCEL
import com.arthenica.mobileffmpeg.FFmpeg.RETURN_CODE_SUCCESS
import com.nicknam.vshareit_for_reddit.ConversionResultReceiver.Companion.KEY_COMMAND_OUTPUT
import com.nicknam.vshareit_for_reddit.ConversionResultReceiver.Companion.KEY_CONTENT_URI
import java.io.File

private const val FILE_PROVIDER_AUTHORITY = "com.nicknam.fileprovider"

/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * helper methods.
 */
class VRedditConvertService : IntentService("VRedditConvertService") {

    override fun onHandleIntent(intent: Intent?) {
        if (intent == null)
            return
        val inputUri: Uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM) ?: return
        val filename = inputUri.pathSegments.first() + ".mp4"
        val videoCachePath = File(externalCacheDir ?: cacheDir, "videos")
        videoCachePath.mkdirs()
        val outputFile = File(videoCachePath, filename)
        val outputUri = Uri.fromFile(outputFile)
        val contentUri: Uri = FileProvider.getUriForFile(baseContext, FILE_PROVIDER_AUTHORITY, outputFile)

        FFmpeg.execute("-y -err_detect ignore_err -i $inputUri -c copy -bsf:a aac_adtstoasc $outputUri")

        val rc = FFmpeg.getLastReturnCode()
        val co = FFmpeg.getLastCommandOutput()

        when (rc) {
            RETURN_CODE_SUCCESS -> Log.i(Config.TAG, "Command execution completed successfully.")
            RETURN_CODE_CANCEL -> Log.i(Config.TAG, "Command execution cancelled by user.")
            else -> Log.e(Config.TAG, String.format("Command execution failed with rc=%d and output=%s.", rc, co))
        }

        val resultReceiver: ResultReceiver = intent.getParcelableExtra(EXTRA_RESULT_RECEIVER)
        resultReceiver.send(rc, Bundle().apply {
            putString(KEY_COMMAND_OUTPUT, co)
            putParcelable(KEY_CONTENT_URI, contentUri)
        })
    }

    companion object {
        const val EXTRA_RESULT_RECEIVER = "RESULT_RECEIVER"
    }
}
