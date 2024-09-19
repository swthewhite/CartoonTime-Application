package com.alltimes.cartoontime.common

interface NumpadAction {
    fun onClickedButton(type: Int)
}

interface PointpadAction {
    fun onPointClickedButton(type: Int)
}