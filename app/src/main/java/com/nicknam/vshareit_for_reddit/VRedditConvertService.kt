package com.nicknam.vshareit_for_reddit

import android.app.IntentService
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.arthenica.mobileffmpeg.FFmpeg.RETURN_CODE_CANCEL
import com.arthenica.mobileffmpeg.FFmpeg.RETURN_CODE_SUCCESS
import java.io.File


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
        val filename = inputUri.path!!.split('/')[1]
        val videoCachePath = File(externalCacheDir ?: cacheDir, "videos")
        val outputFile = File(videoCachePath, "$filename.mp4")
        val outputUri = Uri.fromFile(outputFile)
        val contentUri: Uri = FileProvider.getUriForFile(baseContext, "com.nicknam.fileprovider", outputFile)

        FFmpeg.execute("-y -err_detect ignore_err -i $inputUri -c copy -bsf:a aac_adtstoasc $outputUri")

        val rc = FFmpeg.getLastReturnCode()
        val co = FFmpeg.getLastCommandOutput()
        when (rc) {
            RETURN_CODE_SUCCESS -> {
                Log.i(Config.TAG, "Command execution completed successfully.")
                val shareIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    type = "video/mp4"
                }
                startActivity(Intent.createChooser(shareIntent, resources.getText(R.string.share_label)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            }
            RETURN_CODE_CANCEL -> {
                Log.i(Config.TAG, "Command execution cancelled by user.")
            }
            else -> {
                Log.e(Config.TAG, String.format("Command execution failed with rc=%d and output=%s.", rc, co))
            }
        }
    }
}
