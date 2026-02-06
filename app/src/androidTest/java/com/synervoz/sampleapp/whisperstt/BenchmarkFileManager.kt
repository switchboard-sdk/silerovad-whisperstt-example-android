package com.synervoz.sampleapp.whisperstt

import android.content.Context
import java.io.File

object BenchmarkFileManager {
    private const val RESULTS_FILE = "benchmark_results.txt"

    fun appendLine(context: Context, line: String) {
        val file = File(context.filesDir, RESULTS_FILE)
        file.appendText("$line\n")
    }

    fun readAllLines(context: Context): List<String> {
        val file = File(context.filesDir, RESULTS_FILE)
        return if (file.exists()) {
            file.readLines()
        } else {
            emptyList()
        }
    }

    fun readFullContent(context: Context): String {
        val file = File(context.filesDir, RESULTS_FILE)
        return if (file.exists()) {
            file.readText()
        } else {
            ""
        }
    }

    fun clearFile(context: Context) {
        val file = File(context.filesDir, RESULTS_FILE)
        if (file.exists()) {
            file.delete()
        }
    }

    fun fileExists(context: Context): Boolean {
        val file = File(context.filesDir, RESULTS_FILE)
        return file.exists()
    }
}