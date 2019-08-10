package com.nicknam.vshareit_for_reddit.util

import android.net.Uri
import android.util.Patterns

private const val HTTP_SCHEME = "http://"
private const val HTTPS_SCHEME = "https://"
private const val V_REDDIT_AUTHORITY = "v.redd.it"
private const val DASH_PATH_SEGMENT = "DASHPlaylist.mpd"

fun generateDownloadUri(url: String): Uri {
    val baseUrl = when {
        url.startsWith(HTTP_SCHEME) -> url.replace(HTTP_SCHEME, HTTPS_SCHEME)
        !url.startsWith(HTTPS_SCHEME) -> HTTPS_SCHEME + url
        else -> url
    }
    var uri = Uri.parse(Uri.decode(baseUrl))
    if (uri.lastPathSegment != DASH_PATH_SEGMENT)
        uri = uri.buildUpon()
            .appendPath(DASH_PATH_SEGMENT)
            .build()

    return uri
}

fun validateDownloadUri(uri: Uri): Boolean =
    !Patterns.WEB_URL.matcher(uri.toString()).matches() || uri.authority != V_REDDIT_AUTHORITY || uri.pathSegments.size != 2