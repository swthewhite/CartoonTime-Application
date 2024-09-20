package com.alltimes.cartoontime.data.model
import java.nio.charset.StandardCharsets
import java.util.*

import java.util.UUID

object BLEConstants {
    val UWB_KIOSK_SERVICE_UUID: UUID = UUID.fromString(stringToUUIDFormat("KIOSKCARTOONTIME"))
    val UWB_WITCH_SERVICE_UUID: UUID = UUID.fromString(stringToUUIDFormat("WITCHCARTOONTIME"))

    val CONTROLEE_CHARACTERISTIC_UUID: UUID = UUID.fromString(stringToUUIDFormat("__CONTROLEE_CHAR"))
    val CONTROLLER_CHARACTERISTIC_UUID: UUID = UUID.fromString(stringToUUIDFormat("CONTROLLER_CHAR"))


    val MY_ID_CHARACTERISTIC_UUID: UUID = UUID.fromString(stringToUUIDFormat("______MY_ID_CHAR"))
    val PARTNER_ID_CHARACTERISTIC_UUID: UUID = UUID.fromString(stringToUUIDFormat("_PARTNER_ID_CHAR"))
}

fun stringToUUIDFormat(input: String): String {
    // 입력 문자열이 16바이트를 초과하는지 확인
    if (input.toByteArray(StandardCharsets.UTF_8).size > 16) {
        throw IllegalArgumentException("Input string must be 16 bytes or less")
    }

    // 16바이트가 안되면 패딩
    val paddedInput = input.padEnd(16, '\u0000')

    // 문자열을 유니코드로 변환
    val unicodeBytes = paddedInput.toByteArray(StandardCharsets.UTF_8)

    // 바이트 배열을 16진수 문자열로 변환
    val hexString = unicodeBytes.joinToString("") { "%02x".format(it) }

    // UUID 형식으로 변환
    val uuidFormatted = "${hexString.substring(0, 8)}-" +
            "${hexString.substring(8, 12)}-" +
            "${hexString.substring(12, 16)}-" +
            "${hexString.substring(16, 20)}-" +
            "${hexString.substring(20, 32)}"

    return uuidFormatted
}

fun main() {
    val inputString = "example string"
    val uuidFormat = stringToUUIDFormat(inputString)
    println(uuidFormat)
}