package com.example.fastfruit

import android.content.Intent
import android.os.Bundle
import android.media.MediaPlayer
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class HomeScreen : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)
        startService(Intent(this, MusicService::class.java))

        setupButtons()
    }

    private fun setupButtons() {
        findViewById<View>(R.id.startButton).setOnClickListener {
            startActivity(Intent(this, GameScreen::class.java))
        }

        findViewById<View>(R.id.settingsButton).setOnClickListener {
            ConfigWindow().show(supportFragmentManager, "settings_window")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, MusicService::class.java)) // Para a música ao fechar o app
    }

    override fun onPause() {
        super.onPause()
        stopService(Intent(this, MusicService::class.java))
    }

    override fun onResume() {
        super.onResume()
        startService(Intent(this, MusicService::class.java))
    }

}
