package com.nicknam.vshareit

import android.net.Uri
import android.os.AsyncTask
import android.util.Patterns
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.*

class GenerateDownloadUriTask(private val callback: (uri: Uri?) -> Unit) : AsyncTask<String, Void, Uri?>() {

    override fun doInBackground(vararg params: String?): Uri? {
        val url = params[0] ?: return null
        return generateDownloadUri(url)
    }

    override fun onPostExecute(result: Uri?) {
        super.onPostExecute(result)
        callback(result)
    }

    private fun generateDownloadUri(url: String): Uri? {
        val baseUrl = when {
            url.startsWith(HTTP_SCHEME) -> url.replace(HTTP_SCHEME, HTTPS_SCHEME)
            !url.startsWith(HTTPS_SCHEME) -> HTTPS_SCHEME + url
            else -> url
        }
        val uri = Uri.parse(Uri.decode(baseUrl))
        val output = when (uri.authority) {
            V_REDDIT_AUTHORITY -> fromVReddit(uri)
            REDDIT_AUTHORITY, WWW_REDDIT_AUTHORITY, REDDIT_SHORT_AUTHORITY -> fromReddit(uri)
            else -> null
        }
        return if (output != null && validateUri(output)) output else null
    }

    private fun fromVReddit(input: Uri): Uri {
        return if (input.lastPathSegment != DASH_PATH_SEGMENT)
            input.buildUpon()
                .appendPath(DASH_PATH_SEGMENT)
                .build()
        else
            input
    }

    private fun fromReddit(uri: Uri): Uri? {
        val pathSegments = uri.pathSegments
        val postId: String = when {
            pathSegments.size == 0 -> return null
            pathSegments.size == 1 -> pathSegments[0]
            pathSegments.size > 1 && pathSegments[0] == "comments" -> pathSegments[1]
            pathSegments.size > 3 && pathSegments[0] == "r" -> pathSegments[3]
            else -> return null
        }
        val apiUrl = "https://api.reddit.com/api/info/?id=t3_$postId"
        val json = getJson(apiUrl) ?: return null
        val url = try {
            JSONObject(json)
                .getJSONObject("data")
                .getJSONArray("children")
                .getJSONObject(0)
                .getJSONObject("data")
                .getString("url")
        } catch (e: JSONException) {
            return null
        }
        return generateDownloadUri(url)
    }

    private fun validateUri(uri: Uri): Boolean = Patterns.WEB_URL.matcher(uri.toString()).matches()

    private fun getJson(url: String): String? {
        val connection = try {
            (URL(url).openConnection() as HttpURLConnection).also { it.connect() }
        } catch (e: MalformedURLException) {
            return null
        } catch (e: IOException) {
            return null
        } catch (e: SocketTimeoutException) {
            return null
        }

        val reader = try {
            BufferedReader(InputStreamReader(connection.inputStream))
        } catch (e: IOException) {
            connection.disconnect()
            return null
        } catch (e: UnknownServiceException) {
            connection.disconnect()
            return null
        }

        try {
            val stringBuilder = StringBuilder()
            while (reader.readLine()?.also { stringBuilder.append(it) } != null);
            return stringBuilder.toString()
        } catch (e: IOException) {
            return null
        } finally {
            try {
                reader.close()
            } catch (e: IOException) {
                return null
            } finally {
                connection.disconnect()
            }
        }
    }

    companion object {
        private const val HTTP_SCHEME = "http://"
        private const val HTTPS_SCHEME = "https://"
        private const val V_REDDIT_AUTHORITY = "v.redd.it"
        private const val REDDIT_AUTHORITY = "reddit.com"
        private const val WWW_REDDIT_AUTHORITY = "www.reddit.com"
        private const val REDDIT_SHORT_AUTHORITY = "redd.it"
        private const val DASH_PATH_SEGMENT = "DASHPlaylist.mpd"
    }
}