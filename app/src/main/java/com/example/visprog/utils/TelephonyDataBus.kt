package com.example.visprog.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object TelephonyDataBus {
    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 100)
    val events = _events.asSharedFlow()

    fun emit(data: String) {
        _events.tryEmit(data)
    }
}
