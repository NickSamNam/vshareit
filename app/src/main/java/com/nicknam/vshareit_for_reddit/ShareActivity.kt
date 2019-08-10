package com.nicknam.vshareit_for_reddit

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.util.Patterns
import android.view.WindowManager
import android.widget.Toast

private const val HTTP_SCHEME = "http://"
private const val HTTPS_SCHEME = "https://"
private const val V_REDDIT_AUTHORITY = "v.redd.it"
private const val DASH_PATH_SEGMENT = "DASHPlaylist.mpd"

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
                it.startsWith(HTTP_SCHEME) -> it.replace(HTTP_SCHEME, HTTPS_SCHEME)
                !it.startsWith(HTTPS_SCHEME) -> HTTPS_SCHEME + it
                else -> it
            }
            uri = Uri.parse(Uri.decode(baseUrl))
            if (uri.lastPathSegment != DASH_PATH_SEGMENT)
                uri = uri.buildUpon()
                    .appendPath(DASH_PATH_SEGMENT)
                    .build()

            if (!Patterns.WEB_URL.matcher(uri.toString()).matches() || uri.authority != V_REDDIT_AUTHORITY || uri.pathSegments.size != 2)
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
