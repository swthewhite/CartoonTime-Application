package com.alltimes.cartoontime.ui.handler

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PointPadClickHandler(
    private val context: Context,
    private val isPointExceeded: () -> Unit
) {
    private val _point = MutableStateFlow("")
    val point: StateFlow<String> = _point

    fun onClickedButton(type: Int, balance: Long) {
        if (type == -1) {
            if (_point.value.isNotEmpty()) _point.value = _point.value.dropLast(1)
        } else if (type == -2) {
            if (_point.value.isNotEmpty()) _point.value += "00"
        } else {
            // 비어있을 때 0입력은 무시 해야함
            if (_point.value.isEmpty() && type == 0) return
            _point.value += type.toString()
        }

        // 포인트량 검사
        isPointExceeded()
    }

    fun clearPoint() {
        _point.value = ""
    }

    fun setPoint(value: String) {
        _point.value = value
    }
}