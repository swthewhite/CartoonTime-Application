package com.alltimes.cartoontime.data.model

import java.util.UUID

object BLEConstants {
    val UWB_SERVICE_UUID: UUID = UUID.fromString("8c380000-10bd-4fdb-ba21-1922d6cf860d")
    val CONTROLEE_CHARACTERISTIC_UUID: UUID = UUID.fromString("8c380001-10bd-4fdb-ba21-1922d6cf860d")
    val CONTROLLER_CHARACTERISTIC_UUID: UUID = UUID.fromString("8c380002-10bd-4fdb-ba21-1922d6cf860d")
}