package com.nicknam.vshareit_for_reddit

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.util.Patterns
import android.view.WindowManager
import android.widget.Toast

class ShareActivity : AppCompatActivity() {

    private var url: String = ""

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

//                val shareIntent: Intent = Intent().apply {
//                    action = Intent.ACTION_SEND
//                    putExtra(Intent.EXTRA_STREAM, data)
//                    type = "video/mp4"
//                }
//                startActivity(Intent.createChooser(shareIntent, resources.getText(R.string.share_label)))
    }

    private fun handleSendText(intent: Intent) {
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
            val baseUrl = when {
                it.startsWith("http://") -> it.replace("http://", "https://")
                !it.startsWith("https://") -> "https://$it"
                else -> it
            }

            val uri: Uri = Uri.parse(Uri.decode(baseUrl))
                .buildUpon()
                .appendPath("DASHPlaylist.mpd")
                .build()

            url = uri.toString()

            if (!Patterns.WEB_URL.matcher(url).matches() || uri.authority != "v.redd.it"
                || uri.path?.removePrefix("/")?.split('/')?.size ?: 0 != 2
            )
                Toast.makeText(this, R.string.toast_invalid_url, Toast.LENGTH_SHORT).show()
            else
                CheckHttpConnectionAsyncTask(::httpConnectionCheckCallback).execute(url)
        }
    }

    private fun httpConnectionCheckCallback(responseCode: @ParameterName(name = "responseCode") Int?) {
        when (responseCode) {
            200 -> {
                // TODO: start conversion
                Toast.makeText(this, "CONVERTING", Toast.LENGTH_SHORT).show()
                startService(Intent(this, VRedditConvertService::class.java).apply {
                    putExtra(VRedditConvertService.EXTRA_URL, url)
                })
            }
            403 -> Toast.makeText(this, R.string.toast_video_not_found, Toast.LENGTH_SHORT).show()
            else -> Toast.makeText(this, R.string.toast_server_error, Toast.LENGTH_SHORT).show()
        }
    }
}
