package com.nicknam.vshareit_for_reddit

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.arthenica.mobileffmpeg.FFmpeg.RETURN_CODE_CANCEL
import com.arthenica.mobileffmpeg.FFmpeg.RETURN_CODE_SUCCESS



/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * helper methods.
 */
class VRedditConvertService : IntentService("VRedditConvertService") {
    override fun onHandleIntent(intent: Intent?) {
        if (intent == null)
            return
        val url = intent.getStringExtra(EXTRA_URL) ?: return
        val split = url.split('/')
        val filename = split[split.size - 2]

        FFmpeg.execute("-y -err_detect ignore_err -i $url -c copy -bsf:a aac_adtstoasc $externalCacheDir/$filename.mp4")

        val rc = FFmpeg.getLastReturnCode()
        val output = FFmpeg.getLastCommandOutput()
        when (rc) {
            RETURN_CODE_SUCCESS -> Log.i(Config.TAG, "Command execution completed successfully.")
            RETURN_CODE_CANCEL -> Log.i(Config.TAG, "Command execution cancelled by user.")
            else -> Log.e(Config.TAG, String.format("Command execution failed with rc=%d and output=%s.", rc, output))
        }
    }

    companion object {
        const val EXTRA_URL = "com.nicknam.vshareit_for_reddit.extra.URL"
    }
}
