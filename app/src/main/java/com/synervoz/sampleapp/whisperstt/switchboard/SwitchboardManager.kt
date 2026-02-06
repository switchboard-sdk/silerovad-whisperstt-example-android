package com.synervoz.sampleapp.whisperstt.switchboard

import android.content.Context
import android.util.Log
import com.synervoz.sampleapp.whisperstt.data.TranscriptionItem
import com.synervoz.sampleapp.whisperstt.data.WhisperModel
import com.synervoz.sampleapp.whisperstt.utils.AssetUtils
import com.synervoz.switchboard.sdk.Switchboard
import com.synervoz.switchboard.sdk.SwitchboardResult
import com.synervoz.switchboardonnx.OnnxExtension
import com.synervoz.switchboardsilerovad.SileroVADExtension
import com.synervoz.switchboardwhisper.WhisperExtension
import java.io.File

class SwitchboardManager(
    private val context: Context,
    private val onTranscription: (TranscriptionItem) -> Unit = {},
    private val onVadStateChange: (String) -> Unit = {},
    private val onError: (String) -> Unit = {}
) {

    companion object {
        private const val TAG = "SwitchboardManager"
    }

    private var engineId: String? = null
    private val eventListeners = mutableListOf<Int>()
    private var currentWhisperModel = WhisperModel.TINY
    private val useAudioPlayer = true

    fun initialize(appId: String, appSecret: String): Boolean {
        return try {
            AssetUtils.copyAssetFileToInternal(context, "conversation-clean-mono.wav", "conversation-clean-mono.wav")
            AssetUtils.copyAssetFileToInternal(context, "ggml-tiny.en.bin", "ggml-tiny.en.bin")
            AssetUtils.copyAssetFileToInternal(context, "ggml-base.en.bin", "ggml-base.en.bin")

            Switchboard.loadExtensionLibrary("SwitchboardWhisper")
            Switchboard.loadExtensionLibrary("SwitchboardOnnx")
            Switchboard.loadExtensionLibrary("SwitchboardSileroVAD")

            WhisperExtension.load()
            OnnxExtension.load()
            SileroVADExtension.load()

            val initResult = Switchboard.initialize(
                appId = appId,
                appSecret = appSecret,
                extensions = mapOf(
                    "Whisper" to emptyMap<String, Any>(),
                    "Onnx" to emptyMap<String, Any>(),
                    "SileroVAD" to emptyMap<String, Any>()
                )
            )

            if (initResult.isError) {
                onError("Failed to initialize Switchboard SDK")
                return false
            }

            val configJson = context.assets.open("STTPlayerExample.json").readBytes().decodeToString()
            val result = Switchboard.createEngine(configJson)
            if (result.isError) {
                onError("Failed to create engine")
                return false
            }

            engineId = result.value
            true
        } catch (e: Exception) {
            onError("Initialize failed: ${e.message}")
            false
        }
    }

    fun start(): Boolean {
        val engineId = this.engineId ?: run {
            onError("Engine not initialized")
            return false
        }

        val transcriptionListenerResult = Switchboard.addEventListener(
            objectId = "sttNode",
            eventName = "transcribed"
        ) { _, eventData ->
            handleTranscription(eventData)
        }

        val speechStartedListenerResult = Switchboard.addEventListener(
            objectId = "vadNode",
            eventName = "speechStarted"
        ) { _, _ ->
            onVadStateChange("Speaking")
        }

        val speechEndedListenerResult = Switchboard.addEventListener(
            objectId = "vadNode",
            eventName = "speechEnded"
        ) { _, _ ->
            onVadStateChange("Silence")
        }

        transcriptionListenerResult.value?.let { eventListeners.add(it) }
        speechStartedListenerResult.value?.let { eventListeners.add(it) }
        speechEndedListenerResult.value?.let { eventListeners.add(it) }

        val startResult = Switchboard.callAction(engineId, "start")
        if (startResult.isError) {
            onError("Failed to start engine")
            return false
        }

        if (loadCurrentWhisperModel().isError) {
            onError("Failed to load Whisper model")
            return false
        }

        if (useAudioPlayer) {
            val audioFilePath = File(context.filesDir, "conversation-clean-mono.wav").absolutePath
            val loadResult = Switchboard.callAction(
                objectId = "audioPlayerNode",
                actionName = "load",
                params = mapOf("audioFilePath" to audioFilePath)
            )

            val playResult = Switchboard.callAction("audioPlayerNode", "play")
            Switchboard.setValue("muteNode", "isMuted", true)
            return loadResult.isSuccess && playResult.isSuccess
        }

        return true
    }

    fun stop(): Boolean {
        return try {
            val engineId = this.engineId ?: return true

            val stopResult = Switchboard.callAction(engineId, "stop")
            cleanup()

            if (stopResult.isError) {
                onError("Failed to stop engine")
                return false
            }
            true
        } catch (e: Exception) {
            onError("Stop failed: ${e.message}")
            false
        }
    }

    fun updateVadThreshold(threshold: Float) {
        try {
            Switchboard.setValue("vadNode", "threshold", threshold)
        } catch (e: Exception) {
            onError("Failed to update VAD threshold: ${e.message}")
        }
    }

    fun updateMinSilenceDuration(duration: Int) {
        try {
            Switchboard.setValue("vadNode", "minSilenceDurationMs", duration)
        } catch (e: Exception) {
            onError("Failed to update min silence duration: ${e.message}")
        }
    }

    fun setWhisperModel(model: WhisperModel) {
        try {
            currentWhisperModel = model
            engineId?.let { loadCurrentWhisperModel() }
        } catch (e: Exception) {
            onError("Failed to update Whisper model: ${e.message}")
        }
    }

    fun getVadThreshold(): Float {
        val result = Switchboard.getValue("vadNode", "threshold")
        return if (result.isSuccess) {
            (result.value as? Number)?.toFloat() ?: 0.6f
        } else {
            0.6f
        }
    }

    fun getMinSilenceDurationMs(): Int {
        val result = Switchboard.getValue("vadNode", "minSilenceDurationMs")
        return if (result.isSuccess) {
            (result.value as? Number)?.toInt() ?: 100
        } else {
            100
        }
    }

    private fun handleTranscription(eventData: Any?) {
        val data = eventData as? Map<String, Any> ?: return
        val text = data["text"] as? String ?: ""
        val processingTime = (data["processingTime"] as? Number)?.toLong() ?: -1L

        if (text.isNotEmpty()) {
            val transcriptionItem = TranscriptionItem(text, processingTime)
            onTranscription(transcriptionItem)
        }
    }

    private fun loadCurrentWhisperModel(): SwitchboardResult<String> {
        val modelFileName = when (currentWhisperModel) {
            WhisperModel.BASE -> "ggml-base.en.bin"
            else -> "ggml-tiny.en.bin"
        }
        val modelPath = "${context.filesDir}/$modelFileName"

        return Switchboard.callAction(
            objectId = "sttNode",
            actionName = "loadModel",
            params = mapOf(
                "modelPath" to modelPath,
                "useGPU" to true
            )
        )
    }

    private fun cleanup() {
        eventListeners.forEach { listenerId ->
            try {
                Switchboard.removeEventListener("vadNode", listenerId)
            } catch (e: Exception) {
                try {
                    Switchboard.removeEventListener("sttNode", listenerId)
                } catch (e2: Exception) {
                    Log.w(TAG, "Could not remove listener $listenerId: ${e2.message}")
                }
            }
        }
        eventListeners.clear()
    }
}