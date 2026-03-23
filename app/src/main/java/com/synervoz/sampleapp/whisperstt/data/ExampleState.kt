package com.synervoz.sampleapp.whisperstt.data

data class TranscriptionItem(
    val text: String,
    val processingTimeMs: Long,
    val timestamp: Long = System.currentTimeMillis()
)

enum class WhisperModel(val displayName: String, val fileName: String) {
    TINY("Tiny", "ggml-tiny.en.bin"),
    BASE("Base", "ggml-base.en.bin")
}