package com.synervoz.sampleapp.whisperstt

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.synervoz.sampleapp.whisperstt.data.WhisperModel
import com.synervoz.sampleapp.whisperstt.switchboard.SwitchboardManager
import org.junit.Assert.*
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class WhisperSTTBenchmarkTest {

    @get:Rule
    val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.RECORD_AUDIO)

    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext

    private val MIN_PROCESSING_TIME_MS = 1L
    private val MAX_PROCESSING_TIME_MS = 30000L

    companion object {
        fun addResult(result: BenchmarkResult) {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val line = """
                ========================================
                Model: ${result.model}
                VAD Threshold: ${result.vadThreshold}
                Min Silence Duration: ${result.minSilenceDurationMs} ms
                Processing Time: ${result.processingTimeMs }ms
                Transcription: ${result.transcription}
                Success: ${result.success}
            """.trimIndent()
            BenchmarkFileManager.appendLine(context, line)
        }
    }

    data class BenchmarkResult(
        val model: WhisperModel,
        val vadThreshold: Float,
        val minSilenceDurationMs: Int,
        val processingTimeMs: Long,
        val transcription: String,
        val success: Boolean
    )

    data class TestResult(
        val initialized: Boolean,
        val transcription: String,
        val processingTimeMs: Long,
        val transcriptionCount: Int
    )

    private fun runWhisperTest(
        whisperModel: WhisperModel,
        vadThreshold: Float = 0.6f,
        minSilenceDurationMs: Int = 100,
        timeoutMs: Long = 60000
    ): TestResult {
        var transcriptionText = ""
        var processingTimeMs = 0L
        var transcriptionCount = 0
        val transcriptionLatch = CountDownLatch(1)

        val switchboardManager = SwitchboardManager(
            context = appContext,
            onTranscription = { item ->
                transcriptionText += if (transcriptionCount > 0) " " + item.text else item.text
                processingTimeMs += item.processingTimeMs
                transcriptionCount++
                println("Received transcription: '${item.text}' (${item.processingTimeMs}ms)")

                transcriptionLatch.countDown()
            },
            onVadStateChange = { state ->
                println("VAD State: $state")
            },
            onError = { error ->
                println("Error: $error")
                transcriptionLatch.countDown()
            }
        )

        val initialized = switchboardManager.initialize(
            appId = "",
            appSecret = ""
        )

        if (!initialized) {
            switchboardManager.stop()
            return TestResult(false, "", 0, 0)
        }

        switchboardManager.setWhisperModel(whisperModel)
        switchboardManager.updateVadThreshold(vadThreshold)
        switchboardManager.updateMinSilenceDuration(minSilenceDurationMs)

        if (!switchboardManager.start()) {
            switchboardManager.stop()
            return TestResult(false, "", 0, 0)
        }

        val transcriptionReceived = transcriptionLatch.await(timeoutMs, TimeUnit.MILLISECONDS)

        if (!transcriptionReceived) {
            println("No transcription received within ${timeoutMs}ms timeout")
        }

        switchboardManager.stop()

        addResult(BenchmarkResult(
            model = whisperModel,
            vadThreshold = vadThreshold,
            minSilenceDurationMs = minSilenceDurationMs,
            processingTimeMs = processingTimeMs,
            transcription = transcriptionText,
            success = true
        ))

        return TestResult(true, transcriptionText, processingTimeMs, transcriptionCount)
    }

    @Test
    fun aa_clearPreviousResults() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        BenchmarkFileManager.clearFile(context)
        BenchmarkFileManager.appendLine(context, "=== BENCHMARK RESULTS ===")
        println("Cleared previous benchmark results")
    }

    @Test
    fun tinyModel_minSilence_1000ms() {
        val result = runWhisperTest(
            whisperModel = WhisperModel.TINY,
            vadThreshold = 0.6f,
            minSilenceDurationMs = 1000
        )

        assertTrue("Failed to initialize Switchboard", result.initialized)

        assertTrue(
            "Processing time should be between ${MIN_PROCESSING_TIME_MS}-${MAX_PROCESSING_TIME_MS}ms, but was ${result.processingTimeMs}",
            result.processingTimeMs in MIN_PROCESSING_TIME_MS..MAX_PROCESSING_TIME_MS
        )

        println("Test 1 - Processing time: ${result.processingTimeMs}ms")
        println("Test 1 - Transcription: ${result.transcription}")
    }

    @Test
    fun tinyModel_minSilence_500ms() {
        val result = runWhisperTest(
            whisperModel = WhisperModel.TINY,
            vadThreshold = 0.6f,
            minSilenceDurationMs = 500
        )

        assertTrue("Failed to initialize Switchboard", result.initialized)

        assertTrue(
            "Processing time should be between ${MIN_PROCESSING_TIME_MS}-${MAX_PROCESSING_TIME_MS}ms, but was ${result.processingTimeMs}",
            result.processingTimeMs in MIN_PROCESSING_TIME_MS..MAX_PROCESSING_TIME_MS
        )

        println("Test 2 - Processing time: ${result.processingTimeMs}ms")
        println("Test 2 - Transcription: ${result.transcription}")
    }

    @Test
    fun tinyModel_minSilence_100ms() {
        val result = runWhisperTest(
            whisperModel = WhisperModel.TINY,
            vadThreshold = 0.6f,
            minSilenceDurationMs = 100
        )

        assertTrue("Failed to initialize Switchboard", result.initialized)

        assertTrue(
            "Processing time should be between ${MIN_PROCESSING_TIME_MS}-${MAX_PROCESSING_TIME_MS}ms, but was ${result.processingTimeMs}",
            result.processingTimeMs in MIN_PROCESSING_TIME_MS..MAX_PROCESSING_TIME_MS
        )

        println("Test 3 - Processing time: ${result.processingTimeMs}ms")
        println("Test 3 - Transcription: ${result.transcription}")
    }

    @Test
    fun baseModel_minSilence_1000ms() {
        val result = runWhisperTest(
            whisperModel = WhisperModel.BASE,
            vadThreshold = 0.6f,
            minSilenceDurationMs = 1000
        )

        assertTrue("Fa iled to initialize Switchboard", result.initialized)

        assertTrue(
            "Processing time should be between ${MIN_PROCESSING_TIME_MS}-${MAX_PROCESSING_TIME_MS}ms, but was ${result.processingTimeMs}",
            result.processingTimeMs in MIN_PROCESSING_TIME_MS..MAX_PROCESSING_TIME_MS
        )

        println("Test 4 - Processing time: ${result.processingTimeMs}ms")
        println("Test 4 - Transcription: ${result.transcription}")
    }

    @Test
    fun baseModel_minSilence_500ms() {
        val result = runWhisperTest(
            whisperModel = WhisperModel.BASE,
            vadThreshold = 0.6f,
            minSilenceDurationMs = 500
        )

        assertTrue("Failed to initialize Switchboard", result.initialized)

        assertTrue(
            "Processing time should be between ${MIN_PROCESSING_TIME_MS}-${MAX_PROCESSING_TIME_MS}ms, but was ${result.processingTimeMs}",
            result.processingTimeMs in MIN_PROCESSING_TIME_MS..MAX_PROCESSING_TIME_MS
        )

        println("Test 5 - Processing time: ${result.processingTimeMs}ms")
        println("Test 5 - Transcription: ${result.transcription}")
    }

    @Test
    fun baseModel_minSilence_100ms() {
        val result = runWhisperTest(
            whisperModel = WhisperModel.BASE,
            vadThreshold = 0.6f,
            minSilenceDurationMs = 100
        )

        assertTrue("Failed to initialize Switchboard", result.initialized)

        assertTrue(
            "Processing time should be between ${MIN_PROCESSING_TIME_MS}-${MAX_PROCESSING_TIME_MS}ms, but was ${result.processingTimeMs}",
            result.processingTimeMs in MIN_PROCESSING_TIME_MS..MAX_PROCESSING_TIME_MS
        )

        println("Test 6 - Processing time: ${result.processingTimeMs}ms")
        println("Test 6 - Transcription: ${result.transcription}")
    }

    @Test
    fun zz_printRawResults() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val content = BenchmarkFileManager.readFullContent(context)

        if (content.isEmpty()) {
            println("No benchmark results found in file")
        } else {
            println(content)
        }

        println("=== END OF RESULTS ===")
    }
}