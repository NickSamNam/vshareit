package com.nicknam.vshareit.util

import android.util.Log
import java.io.File

class CacheHelper(private val storageLocations: List<File?>) {

    fun cleanCache() {
        var files = mutableListOf<File>()
        for (storageLocation in storageLocations) {
            val videoCachePath = File(storageLocation, VIDEOS_DIR_NAME)
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

    fun getVideoFile(filename: String): Pair<File?, Boolean> {
        for (storageLocation in storageLocations) {
            if (storageLocation == null)
                continue
            val videoCachePath = File(storageLocation, VIDEOS_DIR_NAME)
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
        Log.e(TAG, "No storage location available.")
        return Pair(null, false)
    }

    fun deleteFile(file: File): Boolean {
        return try {
            file.delete().also { if (!it) Log.e(TAG, "Error deleting file.") }
        } catch (e: SecurityException) {
            Log.e(TAG, "Access denied deleting file.")
            false
        }
    }

    companion object {
        private const val TAG = "CacheHelper"
        private const val CACHE_SIZE_LIMIT = 104857600L
        private const val CACHE_EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000L
        private const val N_MIN_AMOUNT_CACHED = 1

        const val VIDEOS_DIR_NAME = "videos"
    }
}