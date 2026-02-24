package com.example.visprog

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class LocationActivity : AppCompatActivity() {

    private lateinit var latitudeTextView: TextView
    private lateinit var longitudeTextView: TextView
    private lateinit var altitudeTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == LocationService.ACTION_LOCATION_UPDATE) {
                latitudeTextView.text = "Широта: ${intent.getDoubleExtra("latitude", 0.0)}"
                longitudeTextView.text = "Долгота: ${intent.getDoubleExtra("longitude", 0.0)}"
                altitudeTextView.text = "Высота: ${intent.getDoubleExtra("altitude", 0.0)}"
                timeTextView.text = "Время: ${intent.getStringExtra("time")}"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        latitudeTextView = findViewById(R.id.latitudeTextView)
        longitudeTextView = findViewById(R.id.longitudeTextView)
        altitudeTextView = findViewById(R.id.altitudeTextView)
        timeTextView = findViewById(R.id.timeTextView)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val startServiceButton: Button = findViewById(R.id.startServiceButton)
        val stopServiceButton: Button = findViewById(R.id.stopServiceButton)
        val viewLogButton: Button = findViewById(R.id.viewLogButton)

        startServiceButton.setOnClickListener {
            checkLocationSettingsAndStart()
        }

        stopServiceButton.setOnClickListener {
            val stopIntent = Intent(this, LocationService::class.java)
            stopService(stopIntent)
            Toast.makeText(this, "Сервис остановлен", Toast.LENGTH_SHORT).show()
        }

        viewLogButton.setOnClickListener {
            showLogFileContent()
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(locationReceiver, IntentFilter(LocationService.ACTION_LOCATION_UPDATE), RECEIVER_NOT_EXPORTED)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(locationReceiver)
    }

    // 1. Сначала проверяем настройки GPS
    private fun checkLocationSettingsAndStart() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).build()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            // GPS включен, проверяем разрешения
            checkPermissionsAndStartService()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // GPS выключен, но можно включить через диалог
                try {
                    val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                    resolutionForResult.launch(intentSenderRequest)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ошибка при показе диалога
                }
            } else {
                Toast.makeText(this, "Невозможно включить GPS", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val resolutionForResult = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            checkPermissionsAndStartService()
        } else {
            Toast.makeText(this, "Для работы сервиса нужен GPS", Toast.LENGTH_LONG).show()
        }
    }

    // 2. Затем проверяем разрешения
    private fun checkPermissionsAndStartService() {
        val requiredPermissions = mutableListOf<String>()
        requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        requiredPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requiredPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isEmpty()) {
            displayLastKnownLocation()
            startLocationService()
        } else {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    @SuppressLint("MissingPermission")
    private fun displayLastKnownLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val localDateTime = Instant.ofEpochMilli(location.time)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
                val currentTime = localDateTime.format(formatter)

                latitudeTextView.text = "Широта: ${location.latitude}"
                longitudeTextView.text = "Долгота: ${location.longitude}"
                altitudeTextView.text = "Высота: ${location.altitude}"
                timeTextView.text = "Время (последнее известное): $currentTime"
            } else {
                Toast.makeText(this, "Ожидание данных от GPS...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startLocationService() {
        val startIntent = Intent(this, LocationService::class.java)
        startForegroundService(startIntent)
        Toast.makeText(this, "Сервис запущен", Toast.LENGTH_SHORT).show()
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.all { it.value }) {
            displayLastKnownLocation()
            startLocationService()
        } else {
            Toast.makeText(this, "Нет разрешений", Toast.LENGTH_LONG).show()
        }
    }

    // Чтение и показ лога
    private fun showLogFileContent() {
        val file = File(filesDir, "location_log_service.json")
        if (file.exists()) {
            val content = file.readText()
            
            // Показываем в диалоговом окне с прокруткой
            val scrollView = android.widget.ScrollView(this)
            val textView = TextView(this)
            textView.text = if (content.isBlank()) "Файл пуст" else content
            textView.setPadding(32, 32, 32, 32)
            scrollView.addView(textView)

            AlertDialog.Builder(this)
                .setTitle("Логи местоположения")
                .setView(scrollView)
                .setPositiveButton("OK", null)
                .setNeutralButton("Очистить") { _, _ ->
                    file.writeText("")
                    Toast.makeText(this, "Лог очищен", Toast.LENGTH_SHORT).show()
                }
                .show()
        } else {
            Toast.makeText(this, "Файл логов еще не создан", Toast.LENGTH_SHORT).show()
        }
    }
}