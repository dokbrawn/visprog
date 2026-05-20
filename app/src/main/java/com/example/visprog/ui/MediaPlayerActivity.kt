package com.example.visprog.ui

import android.media.MediaPlayer
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.visprog.R

class MediaPlayerActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var trackTitle: TextView
    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_player)

        trackTitle = findViewById(R.id.trackTitleTextView)
        
        findViewById<ImageButton>(R.id.btn_play_pause).setOnClickListener {
            if (isPlaying) {
                pauseMusic()
            } else {
                playMusic()
            }
        }
    }

    private fun playMusic() {
        if (mediaPlayer == null) {
            trackTitle.text = "Воспроизведение... (ресурс не найден)"
        }
        mediaPlayer?.start()
        isPlaying = true
        findViewById<ImageButton>(R.id.btn_play_pause).setImageResource(android.R.drawable.ic_media_pause)
    }

    private fun pauseMusic() {
        mediaPlayer?.pause()
        isPlaying = false
        findViewById<ImageButton>(R.id.btn_play_pause).setImageResource(android.R.drawable.ic_media_play)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
}
