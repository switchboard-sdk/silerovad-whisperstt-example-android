package com.synervoz.sampleapp.whisperstt.utils

import android.content.Context
import android.util.Log
import java.io.File
import java.io.IOException

object AssetUtils {
    fun copyAssetFileToInternal(context: Context, assetPath: String, targetFileName: String): File {
        val outFile = File(context.filesDir, targetFileName)
        try {
            context.assets.open(assetPath).use { input ->
                outFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: IOException) {
            Log.e("AssetUtils", "Failed to copy asset file: $assetPath", e)
        }
        return outFile
    }
}