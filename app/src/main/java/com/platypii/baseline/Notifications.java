package com.platypii.baseline;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat; // TODO: minsdk16
import com.platypii.baseline.data.MyDatabase;

/**
 * Manage notification bars
 */
class Notifications {
    private static final int notificationId = 117;

    static void updateNotification(Context context) {
        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        final boolean logging = MyDatabase.isLogging();
        final boolean audible = Services.audible.isEnabled();

        if(logging || audible) {
            // Show/update notification
            final Intent baselineIntent = new Intent(context, MainActivity.class);
            final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, baselineIntent, 0);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("BASEline Flight Computer")
                    .setOngoing(true)
                    .setContentIntent(pendingIntent);
            if(logging && audible) {
                builder = builder.setUsesChronometer(true).setWhen(MyDatabase.getStartTime());
                builder = builder.setContentText(context.getString(R.string.notify_audible_logging));
            } else if(logging) {
                builder = builder.setUsesChronometer(true).setWhen(MyDatabase.getStartTime());
                builder = builder.setContentText(context.getString(R.string.notify_logging));
            } else {
                builder = builder.setContentText(context.getString(R.string.notify_audible));
            }
            final Notification notification = builder.build();
            notificationManager.notify(notificationId, notification);
        } else {
            // Hide notification
            notificationManager.cancel(notificationId);
        }
    }

}
