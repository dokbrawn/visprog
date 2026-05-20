package com.example.visprog.network

import android.Manifest
import android.content.Context
import android.telephony.*
import androidx.annotation.RequiresPermission
import com.example.visprog.data.MobileNetworkData
import com.example.visprog.data.MobileNetworkDataList

class MobileNetworkManager(private val context: Context) {
    private val telephonyManager: TelephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    private fun validInt(v: Int): Int = if (v == Int.MAX_VALUE) -1 else v
    private fun validLong(v: Long): Long = if (v == Long.MAX_VALUE) -1L else v

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun getMobileNetworkData(): MobileNetworkDataList {
        val networkDataList = mutableListOf<MobileNetworkData>()
        val cellInfoList = telephonyManager.allCellInfo ?: emptyList()

        for (cellInfo in cellInfoList) {
            when (cellInfo) {
                is CellInfoLte -> {
                    val id = cellInfo.cellIdentity
                    val ss = cellInfo.cellSignalStrength
                    networkDataList.add(MobileNetworkData(
                        type = "LTE",
                        cellId = id.ci.toString(),
                        mcc = id.mccString ?: "",
                        mnc = id.mncString ?: "",
                        pci = validInt(id.pci),
                        tac = validInt(id.tac),
                        earfcn = validInt(id.earfcn),
                        band = id.bands.joinToString(","),
                        rsrp = validInt(ss.rsrp),
                        rsrq = validInt(ss.rsrq),
                        rssi = validInt(ss.rssi),
                        rssnr = validInt(ss.rssnr),
                        cqi = validInt(ss.cqi),
                        timingAdvance = validInt(ss.timingAdvance),
                        asuLevel = validInt(ss.asuLevel),
                        dbm = validInt(ss.dbm)
                    ))
                }
                is CellInfoGsm -> {
                    val id = cellInfo.cellIdentity
                    val ss = cellInfo.cellSignalStrength
                    networkDataList.add(MobileNetworkData(
                        type = "GSM",
                        cellId = id.cid.toString(),
                        mcc = id.mccString ?: "",
                        mnc = id.mncString ?: "",
                        arfcn = validInt(id.arfcn),
                        bsic = validInt(id.bsic),
                        lac = validInt(id.lac),
                        dbm = validInt(ss.dbm),
                        rssi = validInt(ss.rssi),
                        timingAdvance = validInt(ss.timingAdvance),
                        asuLevel = validInt(ss.asuLevel)
                    ))
                }
                is CellInfoNr -> {
                    val id = cellInfo.cellIdentity as CellIdentityNr
                    val ss = cellInfo.cellSignalStrength as CellSignalStrengthNr
                    networkDataList.add(MobileNetworkData(
                        type = "NR",
                        cellId = id.nci.toString(),
                        mcc = id.mccString ?: "",
                        mnc = id.mncString ?: "",
                        pci = validInt(id.pci),
                        tac = validInt(id.tac),
                        nrarfcn = validInt(id.nrarfcn),
                        band = id.bands.joinToString(","),
                        rsrp = validInt(ss.ssRsrp),
                        rsrq = validInt(ss.ssRsrq),
                        sinr = if (ss.ssSinr == Int.MAX_VALUE) 0.0 else ss.ssSinr.toDouble(),
                        timingAdvance = validInt(ss.timingAdvanceMicros),
                        asuLevel = validInt(ss.asuLevel),
                        dbm = validInt(ss.dbm)
                    ))
                }
            }
        }
        return MobileNetworkDataList(networkDataList)
    }
}
