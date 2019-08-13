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
import com.nicknam.vshareit_for_reddit.util.CacheHelper

private const val FILE_PROVIDER_AUTHORITY = "com.nicknam.fileprovider"

/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * helper methods.
 */
class VRedditConvertService : IntentService("VRedditConvertService") {

    private val cacheHelper: CacheHelper by lazy { CacheHelper(listOf(externalCacheDir, cacheDir)) }

    override fun onHandleIntent(intent: Intent?) {
        if (intent == null)
            return

        val inputUri: Uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM) ?: return
        val resultReceiver: ResultReceiver? = intent.getParcelableExtra(EXTRA_RESULT_RECEIVER)

        val (outputFile, preexisting) = cacheHelper.getVideoFile(inputUri.pathSegments.first() + ".mp4")
        if (outputFile == null) {
            resultReceiver?.send(ConversionResultReceiver.RESULT_CODE_STORAGE_ERROR, Bundle())
            return
        }
        val outputUri = Uri.fromFile(outputFile)
        val contentUri: Uri = FileProvider.getUriForFile(baseContext, FILE_PROVIDER_AUTHORITY, outputFile)

        val resultCode = if (preexisting) {
            Log.i(TAG, "Video fetched from cache.")
            ConversionResultReceiver.RESULT_CODE_FETCHED_FROM_CACHE
        } else {
            FFmpeg.execute("-y -err_detect ignore_err -i $inputUri -c copy -bsf:a aac_adtstoasc $outputUri")

            val rc = FFmpeg.getLastReturnCode()
            val co = FFmpeg.getLastCommandOutput()

            when (rc) {
                FFmpeg.RETURN_CODE_SUCCESS -> {
                    Log.i(Config.TAG, "Command execution completed successfully.")
                    ConversionResultReceiver.RESULT_CODE_FETCHED_FROM_SOURCE
                }
                FFmpeg.RETURN_CODE_CANCEL -> {
                    Log.i(Config.TAG, "Command execution cancelled by user.")
                    cacheHelper.deleteFile(outputFile)
                    ConversionResultReceiver.RESULT_CODE_OPERATION_CANCELLED
                }
                else -> {
                    Log.e(Config.TAG, String.format("Command execution failed with rc=%d and output=%s.", rc, co))
                    cacheHelper.deleteFile(outputFile)
                    ConversionResultReceiver.RESULT_CODE_FFMPEG_ERROR
                }
            }
        }

        resultReceiver?.send(resultCode, Bundle().apply {
            putParcelable(ConversionResultReceiver.KEY_CONTENT_URI, contentUri)
        })

        cacheHelper.cleanCache()
    }

    companion object {
        private const val TAG = "VRedditConvertService"

        const val EXTRA_RESULT_RECEIVER = "RESULT_RECEIVER"
    }
}
