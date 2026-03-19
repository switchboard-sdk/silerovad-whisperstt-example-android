package com.synervoz.sampleapp.whisperstt.viewmodel

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.synervoz.sampleapp.whisperstt.data.*
import com.synervoz.sampleapp.whisperstt.switchboard.SwitchboardHandler
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
            val stats = systemMonitor.getCurrentStats()
            systemStats.postValue(systemMonitor.formatStats(stats))
            handler.postDelayed(this, 1000)
        }
    }

    private val switchboardHandler = SwitchboardHandler(
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

    init {
        handler.post(statsUpdateRunnable)
        initialize()
    }

    fun initialize() {
        val success = switchboardHandler.initialize("", "")
        if (success) {
            isInitialized.postValue(true)
        }
    }

    fun start() {
        val success = switchboardHandler.start()
        if (success) {
            isRunning.postValue(true)
        }
    }

    fun stop() {
        val success = switchboardHandler.stop()
        if (success) {
            isRunning.postValue(false)
        }
    }

    fun updateVadThreshold(threshold: Float) {
        switchboardHandler.updateVadThreshold(threshold)
    }

    fun updateMinSilenceDuration(duration: Int) {
        switchboardHandler.updateMinSilenceDuration(duration)
    }

    fun setWhisperModel(model: WhisperModel) {
        switchboardHandler.setWhisperModel(model)
    }

    fun getVadThreshold(): Float {
        return switchboardHandler.getVadThreshold()
    }

    fun getMinSilenceDurationMs(): Int {
        return switchboardHandler.getMinSilenceDurationMs()
    }

    fun getWhisperModel(): WhisperModel {
        return switchboardHandler.getWhisperModel()
    }

    fun clearError() {
        error.postValue(null)
    }

    override fun onCleared() {
        super.onCleared()
        handler.removeCallbacks(statsUpdateRunnable)
    }
}