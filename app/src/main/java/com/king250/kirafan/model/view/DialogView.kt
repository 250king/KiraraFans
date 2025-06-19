package com.king250.kirafan.model.view

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DialogView(application: Application) : AndroidViewModel(application) {
    private val _root = MutableStateFlow(false)

    private val _usb = MutableStateFlow(false)

    private val _game = MutableStateFlow(false)

    private val _system = MutableStateFlow(false)

    private val _selector = MutableStateFlow(false)

    private val _next = MutableStateFlow(false)

    val root: StateFlow<Boolean> = _root

    val usb: StateFlow<Boolean> = _usb

    val game: StateFlow<Boolean> = _game

    val system: StateFlow<Boolean> = _system

    val selector: StateFlow<Boolean> = _selector

    val next: StateFlow<Boolean> = _next

    fun openRoot(show: Boolean) {
        _root.value = show
    }

    fun openUsb(show: Boolean) {
        _usb.value = show
    }

    fun openGame(show: Boolean) {
        _game.value = show
    }

    fun openSystem(show: Boolean) {
        _system.value = show
    }

    fun openSelector(show: Boolean) {
        _selector.value = show
    }

    fun openNext(show: Boolean) {
        _next.value = show
    }
}