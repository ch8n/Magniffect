package ch8n.project.magniffect.utils.helpers

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat

class ActivityNotificationBuilder(
        context: Context,
        iconRes:Int,
        contentPending: PendingIntent,
        contentTitle:String,
        contentText:String,
        ticker:String) : NotificationCompat.Builder(context) {
}