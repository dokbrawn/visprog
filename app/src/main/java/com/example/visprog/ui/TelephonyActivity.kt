package com.example.visprog.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.TelephonyManager
import android.text.Html
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.visprog.R
import com.example.visprog.network.ApiClient
import com.example.visprog.network.MobileNetworkManager
import com.example.visprog.services.TelephonyService
import com.example.visprog.utils.TelephonyDataBus
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*

class TelephonyActivity : AppCompatActivity() {
    private val TAG = "TelephonyActivity"
    private lateinit var tvInfo: TextView
    private lateinit var btnStartService: Button
    private lateinit var btnStopService: Button
    private lateinit var etIp: EditText
    private lateinit var btnSetServer: Button
    private lateinit var btnRefreshCells: Button
    
    private lateinit var networkManager: MobileNetworkManager
    private lateinit var apiClient: ApiClient
    private var uiScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_telephony)
        
        val rootView = findViewById<android.view.ViewGroup>(android.R.id.content).getChildAt(0)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        networkManager = MobileNetworkManager(this)
        apiClient = ApiClient(this)
        
        tvInfo = findViewById(R.id.tv_telephony_info)
        btnStartService = findViewById(R.id.btn_start_service)
        btnStopService = findViewById(R.id.btn_stop_service)
        etIp = findViewById(R.id.et_server_ip)
        btnSetServer = findViewById(R.id.btn_set_server)
        btnRefreshCells = findViewById(R.id.btn_refresh_cells)

        etIp.setText(apiClient.getServerAddress() ?: "")
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun onResume() {
        super.onResume()
        
        btnSetServer.setOnClickListener {
            val ip = etIp.text.toString().trim()
            if (ip.isNotEmpty()) {
                apiClient.saveServerAddress(ip)
                Toast.makeText(this, "IP Saved: $ip", Toast.LENGTH_SHORT).show()
            }
        }

        btnStartService.setOnClickListener {
            if (checkPermissions()) {
                val intent = Intent(this, TelephonyService::class.java)
                startForegroundService(intent)
                Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show()
            } else {
                requestPermissions()
            }
        }

        btnStopService.setOnClickListener {
            val intent = Intent(this, TelephonyService::class.java)
            intent.action = "stop"
            startService(intent)
            Toast.makeText(this, "Service Stopped", Toast.LENGTH_SHORT).show()
        }

        btnRefreshCells.setOnClickListener {
            updateTelephonyInfo()
        }

        uiScope = CoroutineScope(Dispatchers.Main + Job())
        uiScope.launch {
            TelephonyDataBus.events.collect { json ->
                updateUIFromJson(json)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        uiScope.cancel()
    }

    private fun updateUIFromJson(json: String) {
        val type = object : TypeToken<Map<String, Any?>>() {}.type
        val data: Map<String, Any?> = com.google.gson.Gson().fromJson(json, type)
        val sb = StringBuilder()

        val location = data["location"] as? Map<*, *>
        if (location != null) {
            sb.append("<font color='#FF9800'><b>LOCATION</b></font><br/>")
            sb.append("Lat: ${location["latitude"]} | Lon: ${location["longitude"]}<br/>")
            sb.append("Alt: ${location["altitude"]} | Acc: ${location["accuracy"]}<br/>")
            sb.append("<br/>")
        }

        val usage = data["networkUsage"] as? Map<*, *>
        if (usage != null) {
            sb.append("<font color='#00BCD4'><b>TRAFFIC</b></font><br/>")
            val sent = (usage["totalBytesSent"] as? Double)?.toLong() ?: 0L
            val recv = (usage["totalBytesReceived"] as? Double)?.toLong() ?: 0L
            sb.append("Sent: ${sent / 1024} KB | Recv: ${recv / 1024} KB<br/>")
            sb.append("<br/>")
        }

        val mobileData = data["mobile_network_data_list"] as? Map<*, *>
        val networks = mobileData?.get("MobileNetworks") as? List<*>
        
        networks?.forEach { item ->
            if (item is Map<*, *>) {
                @Suppress("UNCHECKED_CAST")
                val cell = item as Map<String, Any?>
                sb.append(formatCellMap(cell))
                sb.append("<br/><font color='#555555'>──────────────────────────────────────────</font><br/>")
            }
        }

        tvInfo.text = Html.fromHtml(sb.toString(), Html.FROM_HTML_MODE_LEGACY)
    }

    private fun formatCellMap(cell: Map<String, Any?>): String {
        val type = cell["type"] as? String ?: "Unknown"
        val sb = StringBuilder()

        sb.append("<b><font color='#4CAF50'>$type</font></b><br/>")
        sb.append("MCC/MNC: <b>${cell["mcc"]}/${cell["mnc"]}</b><br/>")
        sb.append("PCI: <b>${cell["pci"]}</b> | TAC: <b>${cell["tac"]}</b><br/>")
        
        when (type) {
            "LTE" -> {
                sb.append("EARFCN: <b>${cell["earfcn"]}</b> | Band: <b>${cell["band"]}</b><br/>")
                sb.append("RSRP: <b>${cell["rsrp"]}</b> | RSRQ: <b>${cell["rsrq"]}</b> | RSSI: <b>${cell["rssi"]}</b><br/>")
                sb.append("RSSI: <b>${cell["rssi"]}</b> | RSSNR: <b>${cell["rssnr"]}</b><br/>")
                sb.append("CQI: <b>${cell["cqi"]}</b> | TA: <b>${cell["timingAdvance"]}</b><br/>")
            }
            "NR" -> {
                sb.append("NCI: <b>${cell["cellId"]}</b> | NRARFCN: <b>${cell["nrarfcn"]}</b> | Band: <b>${cell["band"]}</b><br/>")
                sb.append("SS-RSRP: <b>${cell["rsrp"]}</b> | SS-RSRQ: <b>${cell["rsrq"]}</b><br/>")
                sb.append("SS-SINR: <b>${cell["sinr"]}</b> | TA: <b>${cell["timingAdvance"]}</b><br/>")
            }
            "GSM" -> {
                sb.append("CID: <b>${cell["cellId"]}</b> | LAC: <b>${cell["lac"]}</b> | ARFCN: <b>${cell["arfcn"]}</b><br/>")
                sb.append("RSSI: <b>${cell["rssi"]}</b> | DBM: <b>${cell["dbm"]}</b><br/>")
            }
        }
        return sb.toString()
    }

    private fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.POST_NOTIFICATIONS
        ), 101)
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private fun updateTelephonyInfo() {
        if (!checkPermissions()) {
            requestPermissions()
            return
        }
        val data = networkManager.getMobileNetworkData()
        val sb = StringBuilder()
        data.networks.forEach { net ->
            sb.append("<b>${net.type}</b>: PCI=${net.pci}, RSRP=${net.rsrp}, EARFCN=${net.earfcn}<br/>")
        }
        tvInfo.text = Html.fromHtml(sb.toString(), Html.FROM_HTML_MODE_LEGACY)
    }
}
