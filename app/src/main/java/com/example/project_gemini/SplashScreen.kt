// src/main/java/com/example/project_gemini/SplashScreenActivity.kt
package com.example.project_gemini

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class SplashScreen: AppCompatActivity() {
    private val splashTimeOut: Long = 2000 // 2 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Handler().postDelayed({
            // Start the RegisterActivity after the delay
            startActivity(Intent(this@SplashScreen, Hushh_Home_Screen::class.java))
            finish() // Close the splash screen activity
        }, splashTimeOut)
    }
}
