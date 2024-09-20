package com.alltimes.cartoontime.data.model.uwb

// RangingCallback.kt
interface RangingCallback {
    fun onDistanceMeasured(distance: Float)
}