package com.example.visprog

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.lang.StringBuilder

class TelephonyActivity : AppCompatActivity() {

    private lateinit var tvInfo: TextView
    private lateinit var btnOpenSockets: Button
    private lateinit var btnStartService: Button
    private lateinit var btnStopService: Button
    private val PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_telephony)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvInfo = findViewById(R.id.tv_telephony_info)
        btnOpenSockets = findViewById(R.id.btn_open_sockets)
        btnStartService = findViewById(R.id.btn_start_service)
        btnStopService = findViewById(R.id.btn_stop_service)

        btnOpenSockets.setOnClickListener {
            val intent = Intent(this, SocketsActivity::class.java)
            startActivity(intent)
        }

        btnStartService.setOnClickListener {
            val intent = Intent(this, TelephonyService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }

        btnStopService.setOnClickListener {
            val intent = Intent(this, TelephonyService::class.java)
            stopService(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermissionsAndGetCellInfo()
    }

    private fun checkPermissionsAndGetCellInfo() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.POST_NOTIFICATIONS
        )

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isEmpty()) {
            displayCellInfo()
            checkUsageStatsPermission()
        } else {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    private fun checkUsageStatsPermission() {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), packageName)
        } else {
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), packageName)
        }
        if (mode != AppOpsManager.MODE_ALLOWED) {
            Toast.makeText(this, "Please grant usage access permission", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
    }

    private fun displayCellInfo() {
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val allCellInfo = telephonyManager.allCellInfo
        if (allCellInfo.isNullOrEmpty()) {
            tvInfo.text = "Данные о сотах недоступны"
            return
        }

        val sb = StringBuilder()
        for (info in allCellInfo) {
            when (info) {
                is CellInfoLte -> sb.append(formatLteInfo(info))
                is CellInfoGsm -> sb.append(formatGsmInfo(info))
                is CellInfoNr -> sb.append(formatNrInfo(info))
            }
            sb.append("\n------------------\n")
        }
        tvInfo.text = sb.toString()
    }

    private fun formatLteInfo(info: CellInfoLte): String {
        val id = info.cellIdentity
        val ss = info.cellSignalStrength
        return """
            [LTE Сеть]
            Band: ${if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) id.bands.joinToString() else "N/A"}
            Cell ID: ${id.ci}
            EARFCN: ${id.earfcn}
            MCC: ${id.mccString}
            MNC: ${id.mncString}
            PCI: ${id.pci}
            TAC: ${id.tac}
            
            Сигнал:
            ASU Level: ${ss.asuLevel}
            CQI: ${if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ss.cqi else "N/A"}
            RSRP: ${ss.rsrp}
            RSRQ: ${ss.rsrq}
            RSSI: ${if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ss.rssi else "N/A"}
            RSSNR: ${ss.rssnr}
            Timing Advance: ${ss.timingAdvance}
        """.trimIndent()
    }

    private fun formatGsmInfo(info: CellInfoGsm): String {
        val id = info.cellIdentity
        val ss = info.cellSignalStrength
        return """
            [GSM Сеть]
            Cell ID: ${id.cid}
            BSIC: ${id.bsic}
            ARFCN: ${id.arfcn}
            LAC: ${id.lac}
            MCC: ${id.mccString}
            MNC: ${id.mncString}
            PSC: ${id.psc}
            
            Сигнал:
            Dbm: ${ss.dbm}
            RSSI: ${if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ss.asuLevel else "N/A"}
            Timing Advance: ${ss.timingAdvance}
        """.trimIndent()
    }

    private fun formatNrInfo(info: CellInfoNr): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return "[NR] Не поддерживается ОС"
        
        val id = info.cellIdentity as CellIdentityNr
        val ss = info.cellSignalStrength as CellSignalStrengthNr
        return """
            [5G NR Сеть]
            Band: ${if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) id.bands.joinToString() else "N/A"}
            NCI: ${id.nci}
            PCI: ${id.pci}
            NRARFCN: ${id.nrarfcn}
            TAC: ${id.tac}
            MCC: ${id.mccString}
            MNC: ${id.mncString}
            
            Сигнал:
            SS-RSRP: ${ss.ssRsrp}
            SS-RSRQ: ${ss.ssRsrq}
            SS-SINR: ${ss.ssSinr}
            Timing Advance: N/A
        """.trimIndent()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                displayCellInfo()
                checkUsageStatsPermission()
            } else {
                tvInfo.text = "Нет разрешений для получения данных о сети"
            }
        }
    }
}
