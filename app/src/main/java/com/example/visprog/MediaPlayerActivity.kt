package com.example.visprog

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.io.File

class MediaPlayerActivity : AppCompatActivity() {

    private var titleText: TextView? = null
    private var playButton: ImageButton? = null
    private var prevButton: ImageButton? = null
    private var nextButton: ImageButton? = null
    private var seekBar: SeekBar? = null
    private var volumeBar: SeekBar? = null
    private var trackList: ListView? = null
    private var currentTimeText: TextView? = null
    private var totalTimeText: TextView? = null

    private var player: MediaPlayer? = null
    private var audioManager: AudioManager? = null

    private var musicFiles: Array<File> = emptyArray()
    private var musicTitles: Array<String> = emptyArray()
    private var currentSong = -1

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_player)

        titleText = findViewById(R.id.trackTitleTextView)
        playButton = findViewById(R.id.btn_play_pause)
        prevButton = findViewById(R.id.btn_prev)
        nextButton = findViewById(R.id.btn_next)
        seekBar = findViewById(R.id.trackSeekBar)
        volumeBar = findViewById(R.id.volumeSeekBar)
        trackList = findViewById(R.id.tracksListView)
        currentTimeText = findViewById(R.id.currentTimeText)
        totalTimeText = findViewById(R.id.totalTimeText)

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        setupVolumeControl()

        playButton?.setOnClickListener { onPlayPauseClicked() }
        prevButton?.setOnClickListener { playPreviousSong() }
        nextButton?.setOnClickListener { playNextSong() }

        seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && player?.isPlaying == true) {
                    player?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(bar: SeekBar?) {}
            override fun onStopTrackingTouch(bar: SeekBar?) {}
        })

        checkStoragePermission()
    }

    private fun startUpdatingSeekBar() {
        runnable = Runnable {
            if (player != null && player!!.isPlaying) {
                val currentPosition = player!!.currentPosition
                seekBar?.progress = currentPosition
                currentTimeText?.text = formatTime(currentPosition)
                handler.postDelayed(runnable, 1000)
            }
        }
        handler.post(runnable)
    }

    private fun stopUpdatingSeekBar() {
        if (::runnable.isInitialized) {
            handler.removeCallbacks(runnable)
        }
    }

    private fun formatTime(ms: Int): String {
        val seconds = (ms / 1000) % 60
        val minutes = (ms / (1000 * 60)) % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    private fun onPlayPauseClicked() {
        if (currentSong == -1) {
            Toast.makeText(this, "Выберите песню", Toast.LENGTH_SHORT).show()
            return
        }

        if (player == null) {
            playSongAtIndex(currentSong)
        } else if (player?.isPlaying == true) {
            player?.pause()
            playButton?.setImageResource(android.R.drawable.ic_media_play)
            stopUpdatingSeekBar()
        } else {
            player?.start()
            playButton?.setImageResource(android.R.drawable.ic_media_pause)
            startUpdatingSeekBar()
        }
    }

    private fun stopMusic() {
        stopUpdatingSeekBar()
        player?.apply {
            stop()
            release()
        }
        player = null
        playButton?.setImageResource(android.R.drawable.ic_media_play)
        seekBar?.progress = 0
        titleText?.text = "Название трека"
        currentTimeText?.text = "0:00"
        totalTimeText?.text = "0:00"
        currentSong = -1
    }

    private fun playPreviousSong() {
        if (currentSong <= 0) {
            Toast.makeText(this, "Это первая песня", Toast.LENGTH_SHORT).show()
            return
        }
        playSongAtIndex(currentSong - 1)
    }

    private fun playNextSong() {
        if (currentSong >= musicFiles.size - 1) {
            Toast.makeText(this, "Это последняя песня", Toast.LENGTH_SHORT).show()
            return
        }
        playSongAtIndex(currentSong + 1)
    }

    private fun playSongAtIndex(index: Int) {
        stopMusic()

        currentSong = index
        val file = musicFiles[index]
        val title = musicTitles[index]

        player = MediaPlayer().apply {
            try {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(file.absolutePath)
                setOnPreparedListener {
                    start()
                    titleText?.text = title
                    playButton?.setImageResource(android.R.drawable.ic_media_pause)
                    seekBar?.max = duration
                    seekBar?.progress = 0
                    totalTimeText?.text = formatTime(duration)
                    startUpdatingSeekBar()
                }
                setOnCompletionListener {
                    stopMusic()
                    titleText?.text = "Название трека"
                }
                prepareAsync()
            } catch (e: Exception) {
                Toast.makeText(this@MediaPlayerActivity, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                release()
                player = null
            }
        }
    }

    private fun setupVolumeControl() {
        audioManager?.let { am ->
            val maxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val curVol = am.getStreamVolume(AudioManager.STREAM_MUSIC)

            volumeBar?.max = maxVol
            volumeBar?.progress = curVol

            volumeBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(bar: SeekBar?, vol: Int, fromUser: Boolean) {
                    if (fromUser) {
                        am.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0)
                    }
                }

                override fun onStartTrackingTouch(bar: SeekBar?) {}
                override fun onStopTrackingTouch(bar: SeekBar?) {}
            })
        }
    }

    override fun onPause() {
        super.onPause()
        if (player?.isPlaying == true) {
            player?.pause()
            playButton?.setImageResource(android.R.drawable.ic_media_play)
            stopUpdatingSeekBar()
        }
    }

    override fun onDestroy() {
        stopMusic()
        super.onDestroy()
    }

    private fun checkStoragePermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            loadAllMusic()
        } else {
            permissionLauncher.launch(permission)
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            loadAllMusic()
        } else {
            Toast.makeText(this, "Нужен доступ к музыке", Toast.LENGTH_LONG).show()
        }
    }


    private fun loadAllMusic() {
        val fileList = mutableListOf<File>()
        val titleList = mutableListOf<String>()

        val projection = arrayOf(
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TITLE
        )


        val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).absolutePath
        val folderPath = if (musicDir.endsWith("/")) musicDir else "$musicDir/"


        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DATA} LIKE ?"
        val selectionArgs = arrayOf("${folderPath}%")

        contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            val dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)

            while (cursor.moveToNext()) {
                val path = cursor.getString(dataIndex)
                val title = cursor.getString(titleIndex)
                val file = File(path)

                if (file.exists() && !file.isDirectory) {
                    fileList.add(file)
                    titleList.add(title)
                }
            }
        }

        if (fileList.isEmpty()) {
            Toast.makeText(this, "Музыка не найдена в папке Music/", Toast.LENGTH_LONG).show()
            return
        }

        musicFiles = fileList.toTypedArray()
        musicTitles = titleList.toTypedArray()
        showMusicList()
    }

    private fun showMusicList() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, musicTitles.toList())
        trackList?.adapter = adapter

        trackList?.setOnItemClickListener { _, _, position, _ ->
            playSongAtIndex(position)
        }
    }
}
