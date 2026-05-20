package com.example.visprog.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.telephony.SignalStrength
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.visprog.R
import com.example.visprog.data.LocationData
import com.example.visprog.network.*
import com.example.visprog.utils.TelephonyDataBus
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import org.zeromq.*

class TelephonyService : LifecycleService() {
    private lateinit var apiClient: ApiClient
    private lateinit var netMgr: MobileNetworkManager
    private lateinit var trafMgr: NetworkTrafficManager
    private lateinit var locClient: FusedLocationProviderClient
    private lateinit var telMgr: TelephonyManager
    private var run = false
    private var fGps = true
    private var fLte = true
    private var fNr = true
    private var lat = 0.0; private var lon = 0.0; private var alt = 0.0; private var acc = 0f
    private val ctx = ZContext()
    private var push: ZMQ.Socket? = null
    private var pull: ZMQ.Socket? = null

    override fun onCreate() {
        super.onCreate()
        apiClient = ApiClient(this); netMgr = MobileNetworkManager(this); trafMgr = NetworkTrafficManager(this)
        locClient = LocationServices.getFusedLocationProviderClient(this); telMgr = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        val ch = NotificationChannel("zmq", "ZMQ", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(ch)
        startForeground(2, NotificationCompat.Builder(this, "zmq").setContentTitle("ZMQ Running").setSmallIcon(R.mipmap.ic_launcher).build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (intent?.action == "stop") stopSelf() else if (!run) start()
        return START_STICKY
    }

    private fun start() {
        val ip = apiClient.getServerAddress() ?: return
        run = true
        lifecycleScope.launch(Dispatchers.IO) {
            push = ctx.createSocket(SocketType.PUSH).apply { connect("tcp://$ip:5558") }
            pull = ctx.createSocket(SocketType.PULL).apply { connect("tcp://$ip:5559") }
            launch {
                while (run) {
                    pull?.recvStr(ZMQ.DONTWAIT)?.let { msg ->
                        val j = JSONObject(msg)
                        if (j.optString("type") == "control") {
                            fGps = j.optBoolean("gps", fGps); fLte = j.optBoolean("lte", fLte); fNr = j.optBoolean("nr", fNr)
                        }
                    }
                    delay(500)
                }
            }
            withContext(Dispatchers.Main) { reg() }
        }
    }

    private fun reg() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return
        locClient.requestLocationUpdates(LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000).build(), object : LocationCallback() {
            override fun onLocationResult(res: LocationResult) {
                res.lastLocation?.let { lat = it.latitude; lon = it.longitude; alt = it.altitude; acc = it.accuracy; send() }
            }
        }, null)
        telMgr.registerTelephonyCallback(mainExecutor, object : TelephonyCallback(), TelephonyCallback.SignalStrengthsListener {
            override fun onSignalStrengthsChanged(s: SignalStrength) { send() }
        })
    }

    private fun send() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val r = JSONObject()
                r.put("user", android.os.Build.MODEL)
                if (fGps) r.put("location", LocationData(lat, lon, alt, acc, System.currentTimeMillis()).toJSONObject())
                val nets = netMgr.getMobileNetworkData().networks.filter { (it.type == "LTE" && fLte) || (it.type == "NR" && fNr) || (it.type == "GSM") }
                r.put("mobile_network_data_list", JSONObject().put("MobileNetworks", JSONArray().apply { nets.forEach { put(it.toJSONObject()) } }))
                r.put("networkUsage", trafMgr.getNetworkUsage()); r.put("timestamp", System.currentTimeMillis())
                val raw = r.toString(); push?.send(raw); TelephonyDataBus.emit(raw)
            } catch (e: Exception) { }
        }
    }

    override fun onDestroy() { run = false; ctx.destroy(); super.onDestroy() }
    override fun onBind(intent: Intent): IBinder? { super.onBind(intent); return null }
}
