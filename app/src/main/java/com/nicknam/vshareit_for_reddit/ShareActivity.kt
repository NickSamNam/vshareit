package com.nicknam.vshareit_for_reddit

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.util.Patterns
import android.view.WindowManager
import android.widget.Toast

class ShareActivity : AppCompatActivity() {

    lateinit var uri: Uri

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
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
            val baseUrl = when {
                it.startsWith("http://") -> it.replace("http://", "https://")
                !it.startsWith("https://") -> "https://$it"
                else -> it
            }

            uri = Uri.parse(Uri.decode(baseUrl))
                .buildUpon()
                .appendPath("DASHPlaylist.mpd")
                .build()

            if (!Patterns.WEB_URL.matcher(uri.toString()).matches() || uri.authority != "v.redd.it"
                || uri.path?.removePrefix("/")?.split('/')?.size ?: 0 != 2
            )
                Toast.makeText(this, R.string.toast_invalid_url, Toast.LENGTH_SHORT).show()
            else
                CheckHttpConnectionAsyncTask(::httpConnectionCheckCallback).execute(uri.toString())
        }
    }

    private fun httpConnectionCheckCallback(responseCode: @ParameterName(name = "responseCode") Int?) {
        when (responseCode) {
            200 -> {
                Toast.makeText(this, R.string.toast_fetching_video, Toast.LENGTH_SHORT).show()
                startService(Intent(this, VRedditConvertService::class.java).apply {
                    putExtra(Intent.EXTRA_STREAM, uri)
                })
            }
            403 -> Toast.makeText(this, R.string.toast_video_not_found, Toast.LENGTH_SHORT).show()
            else -> Toast.makeText(this, R.string.toast_server_error, Toast.LENGTH_SHORT).show()
        }
    }
}
