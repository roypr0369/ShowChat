package com.example.showchat.firebase

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.showchat.R
import com.example.showchat.activities.ChatActivity
import com.example.showchat.models.User
import com.example.showchat.utilities.Constants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessagingService : FirebaseMessagingService(){

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    @SuppressLint("UnspecifiedImmutableFlag", "MissingPermission")
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val user = User()
        user.id = message.data[Constants.KEY_USER_ID]!!
        user.name = message.data[Constants.KEY_NAME]!!
        user.token = message.data[Constants.KEY_FCM_TOKEN]!!

        val notificationId: Int = java.util.Random().nextInt()
        val channelId = "chat_message"

        val intent = Intent(this, ChatActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra(Constants.KEY_USER, user)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)!!
        val builder = NotificationCompat.Builder(this, channelId)
        builder.setSmallIcon(R.drawable.ic_notification)
        builder.setContentTitle(user.name)
        builder.setContentText(message.data[Constants.KEY_MESSAGE]!!)
        builder.setStyle(NotificationCompat.BigTextStyle().bigText(
            message.data[Constants.KEY_MESSAGE]!!
        ))
        builder.priority = NotificationCompat.PRIORITY_DEFAULT
        builder.setContentIntent(pendingIntent)
        builder.setAutoCancel(true)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channelName = "Chat Message"
            val channelDescription = "This notification channel is used for chat message notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance)
            channel.description = channelDescription
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationManagerCompat = NotificationManagerCompat.from(this)
        notificationManagerCompat.notify(notificationId, builder.build())
    }
}