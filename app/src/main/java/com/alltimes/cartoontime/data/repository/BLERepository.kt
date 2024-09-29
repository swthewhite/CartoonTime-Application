package com.alltimes.cartoontime.data.repository

import androidx.annotation.RequiresPermission
import com.alltimes.cartoontime.data.network.ble.BLEServer

class BleRepository(private val bleServerManager: BLEServer) {

    @RequiresPermission(allOf = ["android.permission.BLUETOOTH_CONNECT", "android.permission.BLUETOOTH_ADVERTISE"])
    suspend fun startServer() {
        bleServerManager.start()
    }

    @RequiresPermission(allOf = ["android.permission.BLUETOOTH_CONNECT", "android.permission.BLUETOOTH_ADVERTISE"])
    suspend fun stopServer() {
        bleServerManager.stop()
    }
}
