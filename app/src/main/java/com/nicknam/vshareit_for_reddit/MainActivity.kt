package com.nicknam.vshareit_for_reddit

import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.content.Intent



class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (intent?.type?.startsWith("video/mp4") == true) {
            var data: Uri? = intent.data
            if (data == null && Build.VERSION.SDK_INT >= 16 && (intent.clipData?.itemCount ?: 0) > 0)
                data = intent.clipData!!.getItemAt(0).uri
            Log.d("Intent", intent?.toString())
            if (data != null) {
                val shareIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, data)
                    type = "video/mp4"
                }
                startActivity(Intent.createChooser(shareIntent, resources.getText(R.string.share_label)))
            }
        }
    }
}
