package com.nicknam.vshareit

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.nicknam.vshareit.util.share


class ShareActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        when (intent?.action) {
            Intent.ACTION_SEND -> {
                if (intent.type == "text/plain") {
                    handleSendText(intent)
                }
            }
            Intent.ACTION_VIEW -> handleViewIntent(intent)
        }

        finish()
    }

    private fun handleSendText(intent: Intent) {
        share(this, intent.getStringExtra(Intent.EXTRA_TEXT) ?: return)
    }

    private fun handleViewIntent(intent: Intent) {
        share(this, intent.dataString ?: "")
    }
}
