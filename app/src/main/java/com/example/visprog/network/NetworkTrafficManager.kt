package com.example.visprog.network

import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.NetworkCapabilities
import org.json.JSONArray
import org.json.JSONObject
import android.app.usage.NetworkStats
import android.content.pm.PackageManager
import kotlin.math.sqrt

class NetworkTrafficManager(private val context: Context) {
    private val networkStatsManager = context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager

    fun getNetworkUsage(): JSONObject {
        val root = JSONObject()
        try {
            val startTime = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
            val endTime = System.currentTimeMillis()
            
            val bucket = networkStatsManager.querySummaryForDevice(NetworkCapabilities.TRANSPORT_CELLULAR, null, startTime, endTime)
            root.put("totalBytesSent", bucket.txBytes)
            root.put("totalBytesReceived", bucket.rxBytes)
            root.put("topApps", getAnomaliesOnly(startTime, endTime))
        } catch (e: Exception) {
            root.put("error", e.message)
        }
        return root
    }

    private fun getAnomaliesOnly(startTime: Long, endTime: Long): JSONArray {
        val array = JSONArray()
        try {
            val stats = networkStatsManager.querySummary(NetworkCapabilities.TRANSPORT_CELLULAR, null, startTime, endTime)
            val appUsageMap = mutableMapOf<Int, Long>()
            val bucket = NetworkStats.Bucket()
            
            while (stats.hasNextBucket()) {
                stats.getNextBucket(bucket)
                val uid = bucket.uid
                val bytes = bucket.rxBytes + bucket.txBytes
                if (bytes > 1024) {
                    appUsageMap[uid] = (appUsageMap[uid] ?: 0L) + bytes
                }
            }
            stats.close()

            if (appUsageMap.isEmpty()) return array

            val values = appUsageMap.values.toList()
            val mean = values.average()
            val stdDev = if (values.size > 1) {
                sqrt(values.map { Math.pow(it - mean, 2.0) }.sum() / values.size)
            } else 0.0
            
            val threshold = mean + 2 * stdDev
            val pm = context.packageManager
            
            appUsageMap.entries
                .filter { it.value > threshold } 
                .sortedByDescending { it.value }
                .forEach { entry ->
                    val jsonApp = JSONObject()
                    jsonApp.put("packageName", resolvePackageName(pm, entry.key))
                    jsonApp.put("bytes", entry.value)
                    jsonApp.put("isAnomaly", true)
                    array.put(jsonApp)
                }
        } catch (e: Exception) {}
        return array
    }

    private fun resolvePackageName(pm: PackageManager, uid: Int): String {
        return when (uid) {
            -4 -> "Удаленные приложения"
            -5 -> "Раздача интернета (Hotspot)"
            1000 -> "Система Android"
            else -> {
                val packages = pm.getPackagesForUid(uid)
                if (!packages.isNullOrEmpty()) {
                    val packageName = packages[0]
                    try {
                        val info = pm.getApplicationInfo(packageName, 0)
                        pm.getApplicationLabel(info).toString()
                    } catch (e: Exception) { packageName }
                } else "UID: $uid"
            }
        }
    }
}
