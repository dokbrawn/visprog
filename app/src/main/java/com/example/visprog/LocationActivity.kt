package com.example.visprog

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.visprog.services.LocationService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.File

class LocationActivity : AppCompatActivity() {

    private lateinit var latitudeTextView: TextView
    private lateinit var longitudeTextView: TextView
    private lateinit var altitudeTextView: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == LocationService.ACTION_LOCATION_UPDATE) {
                val latitude = intent.getDoubleExtra("latitude", 0.0)
                val longitude = intent.getDoubleExtra("longitude", 0.0)
                val altitude = intent.getDoubleExtra("altitude", 0.0)

                latitudeTextView.text = "Широта: $latitude"
                longitudeTextView.text = "Долгота: $longitude"
                altitudeTextView.text = "Высота: $altitude"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        latitudeTextView = findViewById(R.id.latitudeTextView)
        longitudeTextView = findViewById(R.id.longitudeTextView)
        altitudeTextView = findViewById(R.id.altitudeTextView)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        findViewById<Button>(R.id.startServiceButton).setOnClickListener { checkPermissionsAndStart() }
        findViewById<Button>(R.id.stopServiceButton).setOnClickListener { stopLocationService() }
        findViewById<Button>(R.id.viewLogButton).setOnClickListener { showLogFileContent() }

        updateInitialText()
    }

    private fun checkPermissionsAndStart() {
        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (permissionsToRequest.isEmpty()) {
            startLocationService()
        } else {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            startLocationService()
        } else {
            Toast.makeText(this, "Разрешение на геолокацию необходимо для работы", Toast.LENGTH_LONG).show()
        }
    }

    private fun startLocationService() {
        displayLastKnownLocation()
        
        val startIntent = Intent(this, LocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(startIntent)
        } else {
            startService(startIntent)
        }
        Toast.makeText(this, "Сервис запущен", Toast.LENGTH_SHORT).show()
    }

    private fun stopLocationService() {
        stopService(Intent(this, LocationService::class.java))
        Toast.makeText(this, "Сервис остановлен", Toast.LENGTH_SHORT).show()
        updateInitialText()
    }

    @SuppressLint("MissingPermission")
    private fun displayLastKnownLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                latitudeTextView.text = "Широта: ${location.latitude}"
                longitudeTextView.text = "Долгота: ${location.longitude}"
                altitudeTextView.text = "Высота: ${location.altitude}"
                Toast.makeText(this, "Показано последнее известное местоположение", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Последнее местоположение неизвестно, ожидаем новый сигнал...", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun updateInitialText() {
        latitudeTextView.text = "Широта: (ожидание)"
        longitudeTextView.text = "Долгота: (ожидание)"
        altitudeTextView.text = "Высота: (ожидание)"
    }
    
    private fun showLogFileContent() {
        val file = File(filesDir, "location_log_service.json")
        if (!file.exists() || file.readText().isBlank()) {
            Toast.makeText(this, "Файл логов пуст или еще не создан", Toast.LENGTH_SHORT).show()
            return
        }

        val scrollView = android.widget.ScrollView(this)
        val textView = TextView(this)
        textView.text = file.readText()
        textView.setPadding(32, 32, 32, 32)
        scrollView.addView(textView)

        AlertDialog.Builder(this)
            .setTitle("Локальный лог местоположения")
            .setView(scrollView)
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(LocationService.ACTION_LOCATION_UPDATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(locationReceiver, intentFilter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(locationReceiver, intentFilter)
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(locationReceiver)
    }
}
