package com.example.visprog.data

import org.json.JSONArray
import org.json.JSONObject

data class MobileNetworkData(
    val type: String,
    val cellId: String = "",
    val mcc: String = "",
    val mnc: String = "",
    val pci: Int = -1,
    val tac: Int = 0,
    val earfcn: Int = -1,
    val nrarfcn: Int = -1,
    val arfcn: Int = -1,
    val band: String = "",
    val rsrp: Int = -140,
    val rsrq: Int = 0,
    val rssi: Int = 0,
    val rssnr: Int = Int.MAX_VALUE,
    val cqi: Int = 0,
    val sinr: Double = 0.0,
    val timingAdvance: Int = 0,
    val asuLevel: Int = 0,
    val dbm: Int = -140,
    val bsic: Int = -1,
    val lac: Int = -1,
    val psc: Int = -1
) {
    fun toJSONObject(): JSONObject {
        val json = JSONObject()
        json.put("type", type)
        json.put("cellId", cellId)
        json.put("mcc", mcc)
        json.put("mnc", mnc)
        json.put("pci", pci)
        json.put("tac", tac)
        json.put("earfcn", earfcn)
        json.put("nrarfcn", nrarfcn)
        json.put("arfcn", arfcn)
        json.put("band", band)
        json.put("rsrp", rsrp)
        json.put("rsrq", rsrq)
        json.put("rssi", rssi)
        json.put("rssnr", rssnr)
        json.put("cqi", cqi)
        json.put("sinr", sinr)
        json.put("timingAdvance", timingAdvance)
        json.put("asuLevel", asuLevel)
        json.put("dbm", dbm)
        json.put("bsic", bsic)
        json.put("lac", lac)
        json.put("psc", psc)
        return json
    }
}

data class MobileNetworkDataList(val networks: List<MobileNetworkData>) {
    fun toJSONObject(): JSONObject {
        val root = JSONObject()
        val array = JSONArray()
        networks.forEach { array.put(it.toJSONObject()) }
        root.put("MobileNetworks", array)
        return root
    }
}
