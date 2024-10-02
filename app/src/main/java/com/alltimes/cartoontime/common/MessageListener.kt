package com.alltimes.cartoontime.common

import com.alltimes.cartoontime.data.model.fcm.FcmMessage

interface MessageListener {
    fun onMessageReceived(message: FcmMessage)
}
