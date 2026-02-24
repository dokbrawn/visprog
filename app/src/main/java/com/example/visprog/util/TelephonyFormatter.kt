package com.example.visprog.util

import android.os.Build
import android.telephony.CellInfo
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoNr
import android.telephony.CellIdentityNr
import android.telephony.CellSignalStrengthNr

object TelephonyFormatter {

    fun formatCellInfo(info: CellInfo): String {
        return when (info) {
            is CellInfoLte -> formatLteInfo(info)
            is CellInfoGsm -> formatGsmInfo(info)
            is CellInfoNr -> formatNrInfo(info)
            else -> "Unsupported cell info type"
        }
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
}
