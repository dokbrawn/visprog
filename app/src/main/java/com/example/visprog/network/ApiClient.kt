package com.example.visprog.network

import android.content.Context
import androidx.preference.PreferenceManager
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ApiClient(private val context: Context) {
    companion object {
        private const val BASE_URL_KEY = "server_address"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    fun saveServerAddress(address: String) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(BASE_URL_KEY, address)
            .apply()
    }

    fun getServerAddress(): String? {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(BASE_URL_KEY, null)
    }

    fun isLoggedIn(): Boolean = getServerAddress() != null
    
    fun clearAuthData() {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .remove(BASE_URL_KEY)
            .apply()
    }
}
