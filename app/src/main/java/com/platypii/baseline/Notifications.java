package com.platypii.baseline;

import com.platypii.baseline.events.AudibleEvent;
import com.platypii.baseline.events.LoggingEvent;
import com.platypii.baseline.views.MainActivity;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Manage notification bars
 */
class Notifications {
    static final int notificationId = 117;
    private static final String channelId = "baseline_active_channel_01";

    @Nullable
    private Context context;

    public void start(@NonNull Context context) {
        this.context = context;

        // Prepare channel before any notification can occur
        initChannel();

        // Subscribe to updates
        EventBus.getDefault().register(this);

        // Update initial state
        if (Services.tracks.logger.isLogging()) {
            final Intent service = new Intent(context, ForegroundService.class);
            service.setAction(ForegroundService.ACTION_START_LOGGING);
            context.startService(service);
        }
        if (Services.audible.settings.isEnabled) {
            final Intent service = new Intent(context, ForegroundService.class);
            service.setAction(ForegroundService.ACTION_START_AUDIBLE);
            context.startService(service);
        }
    }

    /**
     * Initialize a notification channel for android 26+
     */
    private void initChannel() {
        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Notification channels require minsdk26
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager != null) {
            // The user-visible name of the channel
            final CharSequence name = context.getString(R.string.app_name);
            // The user-visible description of the channel
            final String description = context.getString(R.string.app_name_long);
            final int importance = NotificationManager.IMPORTANCE_LOW;
            final NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            // Configure the notification channel
            channel.setDescription(description);
            channel.enableLights(true);
            // Sets the notification light color for notifications posted to this channel, if the device supports this feature
            channel.setLightColor(Color.MAGENTA);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Construct a notification object for a given logging and audible state
     */
    @NonNull
    static Notification getNotification(@NonNull Context context, boolean logging, boolean audible) {
        final int flags = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0; // minsdk23
        // Intent to open MainActivity on stop
        final Intent mainIntent = new Intent(context, MainActivity.class);
        final PendingIntent mainPendingIntent = PendingIntent.getActivity(context, 0, mainIntent, flags);
        // Intent to stop logging and audible
        final Intent stopIntent = new Intent(context, ForegroundService.class);
        stopIntent.setAction(ForegroundService.ACTION_CLICK_STOP);
        final PendingIntent stopPendingIntent = PendingIntent.getService(context, 0, stopIntent, flags);
        final NotificationCompat.Action stopAction = new NotificationCompat.Action.Builder(R.drawable.square, "Stop", stopPendingIntent).build();
        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ws_white)
                .setContentTitle(context.getString(R.string.app_name_long))
                .setOngoing(true)
                .setContentIntent(mainPendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(stopAction);
        // Log timer
        if (logging) {
            builder = builder.setUsesChronometer(true).setWhen(Services.tracks.logger.getStartTime());
        }
        // Caption
        if (logging && audible) {
            builder = builder.setContentText(context.getString(R.string.notify_audible_logging));
        } else if (logging) {
            builder = builder.setContentText(context.getString(R.string.notify_logging));
        } else {
            builder = builder.setContentText(context.getString(R.string.notify_audible));
        }
        return builder.build();
    }

    // Subscribe to updates
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoggingEvent(@NonNull LoggingEvent event) {
        final Intent service = new Intent(context, ForegroundService.class);
        if (event.started) {
            service.setAction(ForegroundService.ACTION_START_LOGGING);
        } else {
            service.setAction(ForegroundService.ACTION_STOP_LOGGING);
        }
        context.startService(service);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudibleEvent(@NonNull AudibleEvent event) {
        final Intent service = new Intent(context, ForegroundService.class);
        if (event.started) {
            service.setAction(ForegroundService.ACTION_START_AUDIBLE);
        } else {
            service.setAction(ForegroundService.ACTION_STOP_AUDIBLE);
        }
        context.startService(service);
    }

    public void stop() {
        EventBus.getDefault().unregister(this);
        context = null;
    }
}
