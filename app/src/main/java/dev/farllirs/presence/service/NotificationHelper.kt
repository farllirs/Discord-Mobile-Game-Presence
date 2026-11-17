package dev.farllirs.presence.service
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
object NotificationHelper { const val CHANNEL="detection"; fun create(c:Context,text:String):Notification { val m=c.getSystemService(NotificationManager::class.java); if(Build.VERSION.SDK_INT>=26)m.createNotificationChannel(NotificationChannel(CHANNEL,"Detección local",NotificationManager.IMPORTANCE_LOW)); return Notification.Builder(c,CHANNEL).setSmallIcon(android.R.drawable.ic_media_play).setContentTitle("Farllirs Presence").setContentText(text).setOngoing(true).setOnlyAlertOnce(true).build() } }
