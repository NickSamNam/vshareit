package com.nicknam.vshareit_for_reddit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
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
    }

    private fun onShareClick(v: View) {
        share(this, etUrl.text.toString())
    }
}
