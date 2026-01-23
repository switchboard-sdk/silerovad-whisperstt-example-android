package com.synervoz.sampleapp.whisperstt.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Process
import android.os.SystemClock

class SystemMonitor(private val context: Context) {
    private var lastWallTimeMs = 0L
    private var lastCpuTimeMs = 0L

    data class SystemStats(
        val cpuUsage: Float,
        val memoryUsageMB: Float,
    )

    fun getCurrentStats(): SystemStats {
        val cpuUsage = getCpuUsage()
        val memoryUsage = getMemoryUsage()

        return SystemStats(cpuUsage, memoryUsage)
    }

    private fun getCpuUsage(): Float {
        val wallNow = SystemClock.elapsedRealtime()
        val cpuNow = Process.getElapsedCpuTime()

        if (lastWallTimeMs == 0L) {
            lastWallTimeMs = wallNow
            lastCpuTimeMs = cpuNow
            return 0f
        }

        val wallDelta = wallNow - lastWallTimeMs
        val cpuDelta = cpuNow - lastCpuTimeMs

        lastWallTimeMs = wallNow
        lastCpuTimeMs = cpuNow

        if (wallDelta <= 0) return 0f

        val cores = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)

        // CPU % of TOTAL device capacity for this app
        return ((cpuDelta.toFloat() / wallDelta.toFloat()) * 100f / cores).coerceIn(0f, 100f)
    }

    private fun getMemoryUsage(): Float {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val totalMemory = memoryInfo.totalMem / (1024 * 1024) // Convert to MB
        val availableMemory = memoryInfo.availMem / (1024 * 1024) // Convert to MB
        val usedMemory = totalMemory - availableMemory

        return usedMemory.toFloat()
    }

    fun formatStats(stats: SystemStats): String {
        return "CPU: ${String.format("%.1f", stats.cpuUsage)}% | " +
                "Memory: ${String.format("%.0f", stats.memoryUsageMB)} MB"
    }
}