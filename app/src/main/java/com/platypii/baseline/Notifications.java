package com.platypii.baseline;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat; // TODO: minsdk16
import com.platypii.baseline.data.MyDatabase;
import com.platypii.baseline.events.AudibleEvent;
import com.platypii.baseline.events.LoggingEvent;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Manage notification bars
 */
class Notifications implements Service {
    private static final int notificationId = 117;

    private Context context;

    @Override
    public void start(@NonNull Context context) {
        this.context = context;
        EventBus.getDefault().register(this);
        update();
    }

    private void update() {
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

    // Subscribe to updates
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoggingEvent(LoggingEvent event) {
        update();
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudibleEvent(AudibleEvent event) {
        update();
    }

    @Override
    public void stop() {
        EventBus.getDefault().unregister(this);
        context = null;
    }
}
