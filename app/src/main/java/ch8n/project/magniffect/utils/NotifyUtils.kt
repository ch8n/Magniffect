package ch8n.project.magniffect.utils

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import ch8n.project.magniffect.utils.helpers.ActivityNotificationBuilder

object NotifyUtils {

    fun notifyShot(context: Context, intent: Intent, id: Int, vararg actions: Notification.Action) {
        val resultPendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
//        val builder = ActivityNotificationBuilder(context, resultPendingIntent)
//                .createBuilder()
//        for (action in actions) {
//            builder.addAction(action)
//        }
//        builder.show(id)
    }
}