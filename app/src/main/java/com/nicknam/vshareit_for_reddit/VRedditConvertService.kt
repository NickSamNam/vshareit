package com.nicknam.vshareit_for_reddit

import android.app.IntentService
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.util.Log
import androidx.core.content.FileProvider
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import java.io.File

private const val FILE_PROVIDER_AUTHORITY = "com.nicknam.fileprovider"

/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * helper methods.
 */
class VRedditConvertService : IntentService("VRedditConvertService") {

    private lateinit var storageLocations: List<File?>

    override fun onCreate() {
        super.onCreate()
        storageLocations = listOf(externalCacheDir, cacheDir)
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent == null)
            return

        val inputUri: Uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM) ?: return
        val resultReceiver: ResultReceiver? = intent.getParcelableExtra(EXTRA_RESULT_RECEIVER)

        val (outputFile, preexisting) = getOutputFile(inputUri.pathSegments.first() + ".mp4")
        if (outputFile == null) {
            resultReceiver?.send(ConversionResultReceiver.RESULT_CODE_STORAGE_ERROR, Bundle())
            return
        }
        val outputUri = Uri.fromFile(outputFile)
        val contentUri: Uri = FileProvider.getUriForFile(baseContext, FILE_PROVIDER_AUTHORITY, outputFile)

        val resultCode = if (preexisting) {
            Log.i(TAG, "Video fetched from cache.")
            ConversionResultReceiver.RESULT_CODE_FETCHED_FROM_CACHE
        } else {
            FFmpeg.execute("-y -err_detect ignore_err -i $inputUri -c copy -bsf:a aac_adtstoasc $outputUri")

            val rc = FFmpeg.getLastReturnCode()
            val co = FFmpeg.getLastCommandOutput()

            when (rc) {
                FFmpeg.RETURN_CODE_SUCCESS -> {
                    Log.i(Config.TAG, "Command execution completed successfully.")
                    ConversionResultReceiver.RESULT_CODE_FETCHED_FROM_SOURCE
                }
                FFmpeg.RETURN_CODE_CANCEL -> {
                    Log.i(Config.TAG, "Command execution cancelled by user.")
                    deleteFile(outputFile)
                    ConversionResultReceiver.RESULT_CODE_OPERATION_CANCELLED
                }
                else -> {
                    Log.e(Config.TAG, String.format("Command execution failed with rc=%d and output=%s.", rc, co))
                    deleteFile(outputFile)
                    ConversionResultReceiver.RESULT_CODE_FFMPEG_ERROR
                }
            }
        }

        resultReceiver?.send(resultCode, Bundle().apply {
            putParcelable(ConversionResultReceiver.KEY_CONTENT_URI, contentUri)
        })

        cleanCache()
    }

    private fun getOutputFile(filename: String): Pair<File?, Boolean> {
        for (storageLocation in storageLocations) {
            val videoCachePath = File(storageLocation, "videos")
            try {
                videoCachePath.mkdirs()
            } catch (e: SecurityException) {
                Log.e(TAG, "Access denied creating directories.")
                continue
            }
            val outputFile = File(videoCachePath, filename)
            val preexisting = try {
                outputFile.exists()
            } catch (e: SecurityException) {
                Log.e(TAG, "Access denied reading file.")
                continue
            }
            return Pair(outputFile, preexisting)
        }
        return Pair(null, false)
    }

    private fun deleteFile(file: File): Boolean {
        return try {
            file.delete().also { if (!it) Log.e(TAG, "Error deleting file.") }
        } catch (e: SecurityException) {
            Log.e(TAG, "Access denied deleting file.")
            false
        }
    }

    private fun cleanCache() {
        var files = mutableListOf<File>()
        for (storageLocation in storageLocations) {
            val videoCachePath = File(storageLocation, "videos")
            try {
                videoCachePath.listFiles()?.let { files.addAll(it) }
            } catch (e: SecurityException) {
                Log.e(TAG, "Access denied reading directory.")
            }
        }
        files.sortBy { it.lastModified() }
        var totalSize = files.map { it.length() }.sum()
        files = files.dropLast(N_MIN_AMOUNT_CACHED).toMutableList()
        val currentTime = System.currentTimeMillis()
        files.forEach {
            if (totalSize > CACHE_SIZE_LIMIT || (currentTime - it.lastModified()) > CACHE_EXPIRE_TIME) {
                val fileSize = it.length()
                if (deleteFile(it))
                    totalSize -= fileSize
            }
        }
    }

    companion object {
        private const val TAG = "VRedditConvertService"
        private const val CACHE_SIZE_LIMIT = 104857600L
        private const val CACHE_EXPIRE_TIME = 7*24*60*60*1000L
        private const val N_MIN_AMOUNT_CACHED = 1

        const val EXTRA_RESULT_RECEIVER = "RESULT_RECEIVER"
    }
}
