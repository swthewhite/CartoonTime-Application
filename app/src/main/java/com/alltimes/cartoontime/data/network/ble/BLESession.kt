package com.alltimes.cartoontime.data.network.ble

interface BLESession {
    fun init() {
        // Initialize BLESession
    }
    fun communicate() {
        // Communicate with BLESession
    }
    fun uwbStart() {
        // Start UWB
    }
    fun terminate() {
        // Terminate BLESession
    }
}