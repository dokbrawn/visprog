package com.example.visprog

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.telephony.*
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.gson.Gson
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class TelephonyService : Service() {

    private val TAG = "TelephonyService"
    private val CHANNEL_ID = "TelephonyServiceChannel"
    private val gson = Gson()
    private val scheduler = Executors.newScheduledThreadPool(1)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Telephony Service")
            .setContentText("Collecting telephony data...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(1, notification)

        Log.d(TAG, "TelephonyService started")

        startDataCollection()

        return START_STICKY
    }

    private fun startDataCollection() {
        val dataCollectionTask = Runnable { 
            collectTelephonyData()
            collectLocationData()
            collectNetworkUsageData()
        }
        scheduler.scheduleWithFixedDelay(dataCollectionTask, 0, 15, TimeUnit.SECONDS)
    }

    private fun collectTelephonyData() {
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        val allCellInfo = telephonyManager.allCellInfo
        if (allCellInfo.isNullOrEmpty()) {
            Log.d(TAG, "No cell info available")
            return
        }
        val cellInfoJson = gson.toJson(allCellInfo)
        Log.d(TAG, "Telephony Data: $cellInfoJson")
    }

    private fun collectLocationData() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(5000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val locationJson = gson.toJson(location)
                    Log.d(TAG, "Location Data: $locationJson")
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun collectNetworkUsageData() {
        val networkStatsManager = getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
        try {
            val summaryWifi = networkStatsManager.querySummaryForDevice(NetworkCapabilities.TRANSPORT_WIFI, "", 0, System.currentTimeMillis())
            val summaryWifiJson = gson.toJson(summaryWifi)
            Log.d(TAG, "Network Usage (Wifi): $summaryWifiJson")

            val summaryMobile = networkStatsManager.querySummaryForDevice(NetworkCapabilities.TRANSPORT_CELLULAR, null, 0, System.currentTimeMillis())
            val summaryMobileJson = gson.toJson(summaryMobile)
            Log.d(TAG, "Network Usage (Mobile): $summaryMobileJson")
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to query network usage", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scheduler.shutdown()
        if(this::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
        Log.d(TAG, "TelephonyService stopped")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Telephony Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}