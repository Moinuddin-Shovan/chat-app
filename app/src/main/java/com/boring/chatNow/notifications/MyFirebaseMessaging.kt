package com.boring.chatNow.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.boring.chatNow.ChatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessaging : FirebaseMessagingService()
{
    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)

        val send = p0.data["send"]
        val user = p0.data["user"]
        val sharedPref = getSharedPreferences("Prefs", Context.MODE_PRIVATE)
        val currentOnline = sharedPref.getString("currentUser","none")
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        if (firebaseUser!= null && send == firebaseUser.uid)
        {
            if (currentOnline != user)
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                {
                    sendOreoNotification(p0)
                }
            }
            else{
                sendNotification(p0)
            }
        }
    }

    private fun sendNotification(remoteMessage: RemoteMessage) {
        val user = remoteMessage.data["user"]
        val icon = remoteMessage.data["icon"]
        val title = remoteMessage.data["title"]
        val body = remoteMessage.data["body"]

        val notification = remoteMessage.notification

        val alert = user!!.replace("\\D".toRegex(), "").toInt()
        val intent = Intent(this, ChatActivity::class.java)

        val bundle = Bundle()
        bundle.putString("userID", user)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this,alert, intent, PendingIntent.FLAG_ONE_SHOT)
        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder:NotificationCompat.Builder =  NotificationCompat.Builder(this)
            .setContentIntent(pendingIntent)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(icon!!.toInt())
            .setSound(defaultSound)
            .setAutoCancel(true)

        val notificationToSys = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        var i = 0
        if( alert > 0 ){
            i = alert
        }
        notificationToSys.notify(i, builder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendOreoNotification(remoteMessage: RemoteMessage)
    {
        val user = remoteMessage.data["user"]
        val icon = remoteMessage.data["icon"]
        val title = remoteMessage.data["title"]
        val body = remoteMessage.data["body"]

        val notification = remoteMessage.notification

        val alert = user!!.replace("\\D".toRegex(), "").toInt()
        val intent = Intent(this, ChatActivity::class.java)

        val bundle = Bundle()
        bundle.putString("userID", user)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this,alert, intent, PendingIntent.FLAG_ONE_SHOT)
        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val oreoNotification = OreoNotification(this)

        val builder: Notification.Builder = oreoNotification.getOreoNotification(title, body, pendingIntent, defaultSound, icon)

        var i = 0
        if( alert > 0 ){
            i = alert
        }
        oreoNotification.getmanager!!.notify(i, builder.build())
    }
}