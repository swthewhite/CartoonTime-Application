package com.alltimes.cartoontime.common

import com.alltimes.cartoontime.data.model.FcmMessage

interface MessageListener {
    fun onMessageReceived(message: FcmMessage)
}
