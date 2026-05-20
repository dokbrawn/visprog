package com.example.visprog.ui

import android.Manifest
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.visprog.R
import com.example.visprog.services.LocationService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.File

class LocationActivity : AppCompatActivity() {

    private lateinit var latitudeTextView: TextView
    private lateinit var longitudeTextView: TextView
    private lateinit var altitudeTextView: TextView
    private lateinit var bStart: Button
    private lateinit var bStop: Button
    private lateinit var bLog: Button
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == LocationService.ACTION_LOCATION_UPDATE) {
                val latitude = intent.getDoubleExtra("latitude", 0.0)
                val longitude = intent.getDoubleExtra("longitude", 0.0)
                val altitude = intent.getDoubleExtra("altitude", 0.0)

                latitudeTextView.text = getString(R.string.latitude_format, latitude.toString())
                longitudeTextView.text = getString(R.string.longitude_format, longitude.toString())
                altitudeTextView.text = getString(R.string.altitude_format, altitude.toString())
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_location)

        val rootView = findViewById<android.view.ViewGroup>(android.R.id.content).getChildAt(0)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        latitudeTextView = findViewById(R.id.latitudeTextView)
        longitudeTextView = findViewById(R.id.longitudeTextView)
        altitudeTextView = findViewById(R.id.altitudeTextView)
        bStart = findViewById(R.id.startServiceButton)
        bStop = findViewById(R.id.stopServiceButton)
        bLog = findViewById(R.id.viewLogButton)
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onResume() {
        super.onResume()
        updateUIFromServiceState()
        
        bStart.setOnClickListener { checkPermissionsAndStart() }
        bStop.setOnClickListener { stopLocationService() }
        bLog.setOnClickListener { showLogFileContent() }

        val intentFilter = IntentFilter(LocationService.ACTION_LOCATION_UPDATE)
        ContextCompat.registerReceiver(this, locationReceiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(locationReceiver)
    }

    private fun checkPermissionsAndStart() {
        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
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
            Toast.makeText(this, R.string.location_permission_required, Toast.LENGTH_LONG).show()
        }
    }

    private fun startLocationService() {
        val startIntent = Intent(this, LocationService::class.java)
        startForegroundService(startIntent)
        Toast.makeText(this, R.string.service_started, Toast.LENGTH_SHORT).show()
    }

    private fun stopLocationService() {
        stopService(Intent(this, LocationService::class.java))
        Toast.makeText(this, R.string.service_stopped, Toast.LENGTH_SHORT).show()
        updateInitialText()
    }

    private fun updateUIFromServiceState() {
        if (LocationService.isRunning) {
            LocationService.lastLocationData?.let { data ->
                latitudeTextView.text = getString(R.string.latitude_format, data.latitude.toString())
                longitudeTextView.text = getString(R.string.longitude_format, data.longitude.toString())
                altitudeTextView.text = getString(R.string.altitude_format, data.altitude.toString())
            } ?: run {
                latitudeTextView.text = getString(R.string.service_waiting)
                longitudeTextView.text = ""
                altitudeTextView.text = ""
            }
        } else {
            updateInitialText()
        }
    }
    
    private fun updateInitialText() {
        val waiting = getString(R.string.waiting)
        latitudeTextView.text = getString(R.string.latitude_format, waiting)
        longitudeTextView.text = getString(R.string.longitude_format, waiting)
        altitudeTextView.text = getString(R.string.altitude_format, waiting)
    }
    
    private fun showLogFileContent() {
        val file = File(filesDir, "location_log_service.json")
        if (!file.exists() || file.readText().isBlank()) {
            Toast.makeText(this, R.string.log_empty, Toast.LENGTH_SHORT).show()
            return
        }

        val scrollView = android.widget.ScrollView(this)
        val textView = TextView(this)
        textView.text = file.readText()
        textView.setPadding(32, 32, 32, 32)
        scrollView.addView(textView)

        AlertDialog.Builder(this)
            .setTitle(R.string.log_title)
            .setView(scrollView)
            .setPositiveButton("OK", null)
            .show()
    }
}
