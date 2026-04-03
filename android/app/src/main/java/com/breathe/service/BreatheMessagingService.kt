package com.breathe.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class BreatheMessagingService : FirebaseMessagingService() {
  override fun onMessageReceived(message: RemoteMessage) {
    Log.d("BreatheMessaging", "Push received: ${message.data.keys}")
  }
}
