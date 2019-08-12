package com.nicknam.vshareit_for_reddit

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.nicknam.vshareit_for_reddit.util.share

class MainActivity : AppCompatActivity() {
    private lateinit var btnShare: Button
    private lateinit var etUrl: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnShare = findViewById(R.id.btn_share)
        etUrl = findViewById(R.id.et_url)

        btnShare.setOnClickListener(this::onShareClick)
        etUrl.setOnEditorActionListener(this::onUrlEditorAction)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onShareClick(v: View) {
        share(this, etUrl.text.toString())
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onUrlEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
        return when (actionId) {
            EditorInfo.IME_ACTION_GO -> {
                share(this, etUrl.text.toString())
                true
            }
            else -> false
        }
    }
}
