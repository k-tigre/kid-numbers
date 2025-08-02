package by.tigre.numbers

import by.tigre.tools.logger.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d { "onMessageReceived = ${message.messageId} -- ${message.notification} -- ${message.data}" }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d { "onNewToken = $token" }
    }
}