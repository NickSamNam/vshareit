package com.nicknam.vshareit_for_reddit

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.view.WindowManager
import android.widget.Toast
import com.nicknam.vshareit_for_reddit.util.generateDownloadUri
import com.nicknam.vshareit_for_reddit.util.validateDownloadUri


class ShareActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        when (intent?.action) {
            Intent.ACTION_SEND -> {
                if (intent.type == "text/plain") {
                    handleSendText(intent)
                }
            }
        }

        finish()
    }

    private fun handleSendText(intent: Intent) {
        val baseUrl = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return
        val downloadUri = generateDownloadUri(baseUrl)
        if (!validateDownloadUri(downloadUri))
            Toast.makeText(this, R.string.toast_invalid_url, Toast.LENGTH_SHORT).show()
        else {
            CheckHttpConnectionAsyncTask {
                when (it) {
                    200 -> {
                        Toast.makeText(this, R.string.toast_fetching_video, Toast.LENGTH_SHORT).show()
                        startConversion(downloadUri)
                    }
                    403 -> Toast.makeText(this, R.string.toast_video_not_found, Toast.LENGTH_SHORT).show()
                    else -> Toast.makeText(this, R.string.toast_server_error, Toast.LENGTH_SHORT).show()
                }
            }.execute(downloadUri.toString())
        }
    }

    private fun startConversion(downloadUri: Uri) {
        startService(Intent(this, VRedditConvertService::class.java).apply {
            putExtra(Intent.EXTRA_STREAM, downloadUri)
        })
    }
}
