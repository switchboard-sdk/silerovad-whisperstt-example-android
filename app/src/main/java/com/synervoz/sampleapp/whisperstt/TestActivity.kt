package com.synervoz.sampleapp.whisperstt

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity

/**
 * Minimal activity for running instrumented tests with foreground priority.
 * This ensures tests run with proper audio routing, CPU scheduling, and
 * system resource allocation similar to a real app in the foreground.
 */
class TestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = View(this)
        setContentView(view)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}