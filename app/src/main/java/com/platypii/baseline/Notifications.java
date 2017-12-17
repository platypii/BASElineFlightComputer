package com.platypii.baseline;

import com.platypii.baseline.events.AudibleEvent;
import com.platypii.baseline.events.LoggingEvent;
import com.platypii.baseline.views.MainActivity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Manage notification bars
 */
class Notifications implements BaseService {
    private static final String TAG = "Notifications";
    static final int notificationId = 117;

    private Context context;

    @Override
    public void start(@NonNull Context context) {
        this.context = context;
        EventBus.getDefault().register(this);

        // Update initial state
        if (Services.logger.isLogging()) {
            final Intent service = new Intent(context, ForegroundService.class);
            service.setAction(ForegroundService.ACTION_START_LOGGING);
            context.startService(service);
        }
        if (Services.audible.isEnabled()) {
            final Intent service = new Intent(context, ForegroundService.class);
            service.setAction(ForegroundService.ACTION_START_AUDIBLE);
            context.startService(service);
        }
    }

    static Notification getNotification(@NonNull Context context, boolean logging, boolean audible) {
        // Intent to open MainActivity on stop
        final Intent mainIntent = new Intent(context, MainActivity.class);
        final PendingIntent mainPendingIntent = PendingIntent.getActivity(context, 0, mainIntent, 0);
        Notification.Builder builder = new Notification.Builder(context)
                .setSmallIcon(R.drawable.ws_white)
                .setContentTitle(context.getString(R.string.app_name_long))
                .setOngoing(true)
                .setContentIntent(mainPendingIntent);
        // Add stop action
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            final Intent stopIntent = new Intent(context, MainActivity.class);
            stopIntent.setType("baseline/stop");
            final PendingIntent stopPendingIntent = PendingIntent.getActivity(context, 0, stopIntent, 0);
            final Notification.Action stopAction = new Notification.Action.Builder(R.drawable.square, "Stop", stopPendingIntent).build();
            builder = builder.addAction(stopAction);
        }
        if(logging && audible) {
            builder = builder.setUsesChronometer(true).setWhen(Services.logger.getStartTime());
            builder = builder.setContentText(context.getString(R.string.notify_audible_logging));
        } else if(logging) {
            builder = builder.setUsesChronometer(true).setWhen(Services.logger.getStartTime());
            builder = builder.setContentText(context.getString(R.string.notify_logging));
        } else {
            builder = builder.setContentText(context.getString(R.string.notify_audible));
        }
        return builder.build();
    }

    // Subscribe to updates
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoggingEvent(LoggingEvent event) {
        final Intent service = new Intent(context, ForegroundService.class);
        if(event.started) {
            service.setAction(ForegroundService.ACTION_START_LOGGING);
        } else {
            service.setAction(ForegroundService.ACTION_STOP_LOGGING);
        }
        context.startService(service);
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudibleEvent(AudibleEvent event) {
        final Intent service = new Intent(context, ForegroundService.class);
        if(event.started) {
            service.setAction(ForegroundService.ACTION_START_AUDIBLE);
        } else {
            service.setAction(ForegroundService.ACTION_STOP_AUDIBLE);
        }
        context.startService(service);
    }

    @Override
    public void stop() {
        EventBus.getDefault().unregister(this);
        context = null;
    }
}
