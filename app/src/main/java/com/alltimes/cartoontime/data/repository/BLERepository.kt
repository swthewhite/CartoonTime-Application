package com.alltimes.cartoontime.data.repository

import androidx.annotation.RequiresPermission
import com.alltimes.cartoontime.data.network.ble.BLEServerManager

class BleRepository(private val bleServerManager: BLEServerManager) {

    @RequiresPermission(allOf = ["android.permission.BLUETOOTH_CONNECT", "android.permission.BLUETOOTH_ADVERTISE"])
    suspend fun startServer() {
        bleServerManager.startServer()
    }

    @RequiresPermission(allOf = ["android.permission.BLUETOOTH_CONNECT", "android.permission.BLUETOOTH_ADVERTISE"])
    suspend fun stopServer() {
        bleServerManager.stopServer()
    }
}
