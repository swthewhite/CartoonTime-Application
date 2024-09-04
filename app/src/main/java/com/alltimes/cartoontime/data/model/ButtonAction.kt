package com.alltimes.cartoontime.data.model

data class ButtonAction(val actionType: ActionType)

enum class ActionType {
    SEND,
    RECEIVE
}