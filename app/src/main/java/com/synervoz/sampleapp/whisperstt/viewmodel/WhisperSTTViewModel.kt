package com.synervoz.sampleapp.whisperstt.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.synervoz.sampleapp.whisperstt.data.*
import com.synervoz.sampleapp.whisperstt.switchboard.SwitchboardManager

class WhisperSTTViewModel(application: Application) : AndroidViewModel(application) {

    val transcriptions = MutableLiveData<List<TranscriptionItem>>(emptyList())
    val vadState = MutableLiveData<String>("--")
    val isRunning = MutableLiveData<Boolean>(false)
    val isInitialized = MutableLiveData<Boolean>(false)
    val error = MutableLiveData<String?>()

    private val switchboardManager = SwitchboardManager(
        context = application,
        onTranscription = { transcription ->
            val current = transcriptions.value ?: emptyList()
            transcriptions.value = current + transcription
        },
        onVadStateChange = { state ->
            vadState.value = state
        },
        onError = { errorMsg ->
            error.value = errorMsg
        }
    )

    fun initialize() {
        val success = switchboardManager.initialize("your_app_id", "your_app_secret")
        if (success) {
            isInitialized.value = true
        }
    }

    fun start() {
        val success = switchboardManager.start()
        if (success) {
            isRunning.value = true
        }
    }

    fun stop() {
        val success = switchboardManager.stop()
        if (success) {
            isRunning.value = false
        }
    }

    fun updateVadThreshold(threshold: Float) {
        switchboardManager.updateVadThreshold(threshold)
    }

    fun updateMinSilenceDuration(duration: Int) {
        switchboardManager.updateMinSilenceDuration(duration)
    }

    fun setWhisperModel(model: WhisperModel) {
        switchboardManager.setWhisperModel(model)
    }

    fun clearError() {
        error.value = null
    }
}