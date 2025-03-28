package com.example.fastfruit

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)
        splashScreen()
    }

    private fun splashScreen(){
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this,HomeScreen::class.java)
            startActivity(intent)
            finish()
        },3000)
    }
}