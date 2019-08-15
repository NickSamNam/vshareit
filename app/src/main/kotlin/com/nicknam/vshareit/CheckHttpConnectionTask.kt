package com.nicknam.vshareit

import android.os.AsyncTask
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class CheckHttpConnectionTask(private val callback: (responseCode: Int?) -> Unit) :
    AsyncTask<String, Void, Int>() {

    override fun doInBackground(vararg params: String?): Int {
        val url = URL(params[0])
        val connection = url.openConnection() as HttpURLConnection
        return try {
            connection.responseCode
        } catch (e: IOException) {
            -2
        }
    }

    override fun onPostExecute(result: Int?) {
        super.onPostExecute(result)
        callback(result)
    }
}