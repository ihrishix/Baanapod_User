package com.decodex.bannapod

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Geocoder
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*
import kotlin.random.Random

private const val CHANNEL_ID = "Firebase Notification"

class Firebase_Service: FirebaseMessagingService(){
    //Runs when a message is received over FCM
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification_ID = Random.nextInt()

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createnotificationchannel(notificationManager)
        }

        val MainActivity_Intent = Intent(this, MainActivity::class.java)
        MainActivity_Intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        //When User Taps on Notification it takes it to Main Activity
        val pendingIntent = PendingIntent.getActivity(this, 0, MainActivity_Intent, FLAG_ONE_SHOT)

        //todo what to do of notfication
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(message.data["phone_no"])
            .setContentText("ABCD")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()
        notificationManager.notify(notification_ID, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createnotificationchannel(notificationManager: NotificationManager){
        val channelname = "Firebase Notification"
        val channel = NotificationChannel(CHANNEL_ID, channelname, IMPORTANCE_HIGH).apply {
            description = "Firebase Notification Channel. For Emergency Notifications"
            enableLights(true)
            lightColor = Color.GREEN
        }
        notificationManager.createNotificationChannel(channel)
    }

}