package com.nicknam.vshareit_for_reddit

import android.os.AsyncTask
import java.net.HttpURLConnection
import java.net.URL

class CheckHttpConnectionAsyncTask(private val callback: (responseCode: Int?) -> Unit) : AsyncTask<String, Void, Int>() {
    override fun doInBackground(vararg params: String?): Int {
        val url = URL(params[0])
        val connection = url.openConnection() as HttpURLConnection
        return connection.responseCode
    }

    override fun onPostExecute(result: Int?) {
        super.onPostExecute(result)
        callback(result)
    }
}