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

class SwitchboardHandler(
    private val context: Context,
    private val onTranscription: (TranscriptionItem) -> Unit = {},
    private val onVadStateChange: (String) -> Unit = {},
    private val onError: (String) -> Unit = {}
) {
    private var engineId: String? = null
    private val vadEventListeners = mutableListOf<Int>()
    private val sttEventListeners = mutableListOf<Int>()
    private var currentWhisperModel = WhisperModel.TINY

    fun initialize(appId: String, appSecret: String): Boolean {
        AssetUtils.copyAssetFileToInternal(context, "ggml-tiny.en.bin", "ggml-tiny.en.bin")
        AssetUtils.copyAssetFileToInternal(context, "ggml-base.en.bin", "ggml-base.en.bin")

        Switchboard.loadExtensionLibrary("SwitchboardWhisper")
        Switchboard.loadExtensionLibrary("SwitchboardOnnx")
        Switchboard.loadExtensionLibrary("SwitchboardSileroVAD")

        WhisperExtension.load()
        OnnxExtension.load()
        SileroVADExtension.load()

        val initResult = Switchboard.initialize(
            context = context,
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

        val configJson = context.assets.open("STTAudioGraph.json").readBytes().decodeToString()
        val result = Switchboard.createEngine(configJson)
        if (result.isError) {
            onError("Failed to create engine")
            return false
        }

        engineId = result.value
        return true
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

        transcriptionListenerResult.value?.let { sttEventListeners.add(it) }
        speechStartedListenerResult.value?.let { vadEventListeners.add(it) }
        speechEndedListenerResult.value?.let { vadEventListeners.add(it) }

        val startResult = Switchboard.callAction(engineId, "start")
        if (startResult.isError) {
            onError("Failed to start engine")
            return false
        }

        if (loadCurrentWhisperModel().isError) {
            onError("Failed to load Whisper model")
            return false
        }

        return true
    }

    fun stop(): Boolean {
        val engineId = this.engineId ?: return true
        Switchboard.callAction(engineId, "stop")
        cleanup()
        return true
    }

    fun updateVadThreshold(threshold: Float) {
        Switchboard.setValue("vadNode", "threshold", threshold)
    }

    fun updateMinSilenceDuration(duration: Int) {
        Switchboard.setValue("vadNode", "minSilenceDurationMs", duration)
    }

    fun setWhisperModel(model: WhisperModel) {
        currentWhisperModel = model
        engineId?.let { loadCurrentWhisperModel() }
    }

    fun getVadThreshold(): Float {
        val result = Switchboard.getValue("vadNode", "threshold")
        return (result.value as? Number)?.toFloat() ?: 0.6f
    }

    fun getMinSilenceDurationMs(): Int {
        val result = Switchboard.getValue("vadNode", "minSilenceDurationMs")
        return  (result.value as? Number)?.toInt() ?: 100
    }

    fun getWhisperModel(): WhisperModel {
        return currentWhisperModel
    }

    private fun handleTranscription(eventData: Any?) {
        val data = eventData as? Map<String, Any> ?: return
        val text = data["text"] as? String ?: ""
        val processingTime = (data["processingTime"] as? Number)?.toLong() ?: -1L

        if (text.isNotEmpty()) {
            Log.d("SwitchboardManager", "$text : $processingTime")
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
        vadEventListeners.forEach { listenerId ->
            Switchboard.removeEventListener("vadNode", listenerId)
        }
        vadEventListeners.clear()

        sttEventListeners.forEach { listenerId ->
            Switchboard.removeEventListener("sttNode", listenerId)
        }
        sttEventListeners.clear()
    }
}