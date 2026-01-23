package com.synervoz.sampleapp.whisperstt.viewmodel

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.synervoz.sampleapp.whisperstt.data.*
import com.synervoz.sampleapp.whisperstt.switchboard.SwitchboardManager
import com.synervoz.sampleapp.whisperstt.utils.SystemMonitor

class WhisperSTTViewModel(application: Application) : AndroidViewModel(application) {

    val transcriptions = MutableLiveData<List<TranscriptionItem>>(emptyList())
    val vadState = MutableLiveData<String>("--")
    val isRunning = MutableLiveData<Boolean>(false)
    val isInitialized = MutableLiveData<Boolean>(false)
    val error = MutableLiveData<String?>()
    val systemStats = MutableLiveData<String>("CPU: -- | Memory: --")

    private val systemMonitor = SystemMonitor(application)
    private val handler = Handler(Looper.getMainLooper())

    private val statsUpdateRunnable = object : Runnable {
        override fun run() {
            if (isRunning.value == true) {
                val stats = systemMonitor.getCurrentStats()
                systemStats.postValue(systemMonitor.formatStats(stats))
            }
            handler.postDelayed(this, 1000)
        }
    }

    private val switchboardManager = SwitchboardManager(
        context = application,
        onTranscription = { transcription ->
            val current = transcriptions.value ?: emptyList()
            transcriptions.postValue(current + transcription)
        },
        onVadStateChange = { state ->
            vadState.postValue(state)
        },
        onError = { errorMsg ->
            error.postValue(errorMsg)
        }
    )

    fun initialize() {
        val success = switchboardManager.initialize("", "")
        if (success) {
            isInitialized.postValue(true)
        }
    }

    fun start() {
        val success = switchboardManager.start()
        if (success) {
            isRunning.postValue(true)
            handler.post(statsUpdateRunnable)
        }
    }

    fun stop() {
        val success = switchboardManager.stop()
        if (success) {
            isRunning.postValue(false)
            handler.removeCallbacks(statsUpdateRunnable)
            systemStats.postValue("CPU: -- | Memory: --")
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
        error.postValue(null)
    }
}