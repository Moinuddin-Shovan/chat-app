package com.boring.chatNow.notifications

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi

class OreoNotification(base: Context?) : ContextWrapper(base)
{
    private var notificationManager: NotificationManager? = null

    companion object{
        private const val channel_ID = "com.boring.chatNow"
        private const val channel_name = "Chit Chat"
    }
    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            createChannel()
        }
    }


    @TargetApi(Build.VERSION_CODES.O)
    private fun createChannel()
    {
        val channel = NotificationChannel(
            channel_ID, channel_name, NotificationManager.IMPORTANCE_DEFAULT)
        channel.enableLights(false)
        channel.enableVibration(true)
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        getmanager!!.createNotificationChannel(channel)
    }

    val getmanager: NotificationManager? get()
    {
        if (notificationManager == null)
        {
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }
        return notificationManager
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun getOreoNotification(
        title: String? ,
        body: String?,
        pendingIntent: PendingIntent?,
        soundUri: Uri?,
        icon: String?): Notification.Builder
    {
        return Notification.Builder(applicationContext, channel_ID)
            .setContentIntent(pendingIntent)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(icon!!.toInt())
            .setSound(soundUri)
            .setAutoCancel(true)
    }
}