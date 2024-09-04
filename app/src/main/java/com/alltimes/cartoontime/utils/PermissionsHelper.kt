package com.alltimes.cartoontime.utils

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat

object PermissionsHelper {
    private val requiredPermissions =
        arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE
        )

    fun hasAllPermissions(activity: ComponentActivity): Boolean {
        return requiredPermissions.all {
            ActivityCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestPermissions(activity: ComponentActivity) {
        ActivityCompat.requestPermissions(activity, requiredPermissions, 1)
    }
}
