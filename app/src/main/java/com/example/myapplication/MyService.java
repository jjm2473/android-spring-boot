package com.example.myapplication;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;


//import com.example.simple_web.Application;

import androidx.annotation.Nullable;

import com.test.demo.Application;

import org.springframework.boot.logging.LoggingSystem;

import javax.management.MBeanServerFactory;

public class MyService extends Service {
    private static final String TAG = "MyService";
    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        setupNotificationChannel();
        startForeground(1337, buildNotification());
        //((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(1337, buildNotification());
        Log.d(TAG, "start spring boot");
        try {
            Hook.init(getApplicationContext());
        } catch (Exception e) {
            Log.e(TAG, "Hook init failed", e);
            this.stopSelf();
            return;
        }
        System.setProperty("user.home", this.getApplicationContext().getFilesDir().getAbsolutePath());
        // disable logger
        System.setProperty(LoggingSystem.SYSTEM_PROPERTY, LoggingSystem.NONE);
        // for org.apache.tomcat.util.modeler.Registry.getMBeanServer
        MBeanServerFactory.createMBeanServer();
        Application.main(new String[0]);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: " + intent.getAction());
        if ("com.example.myapplication.stop_http_server".equals(intent.getAction())) {
            stopForeground(true);
            stopSelf();
            System.exit(0);
        }
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    private Notification buildNotification() {
        Resources res = getResources();

        // Set pending intent to be launched when notification is clicked
        Intent notificationIntent = MainActivity.newInstance(this);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        String notificationText = "keep alive";



        // Set notification priority
        // If holding a wake or wifi lock consider the notification of high priority since it's using power,
        // otherwise use a low priority
        int priority =  Notification.PRIORITY_LOW;


        // Build the notification
        Notification.Builder builder =  geNotificationBuilder(this,
                "springboot_notification_channel", priority,
                "HTTP SERVER", notificationText, null,
                contentIntent, null);
        if (builder == null)  return null;

        // No need to show a timestamp:
        builder.setShowWhen(false);

        // Set notification icon
        builder.setSmallIcon(R.drawable.ic_launcher_foreground);

        // Set background color for small notification icon
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setColor(0xFF607D8B);
        }

        // TermuxSessions are always ongoing
        builder.setOngoing(true);


        // Set Exit button action
        Intent exitIntent = new Intent(this, MyService.class).setAction("com.example.myapplication.stop_http_server");
        builder.addAction(android.R.drawable.ic_delete, res.getString(R.string.exit_http), PendingIntent.getService(this, 0, exitIntent, 0));

        return builder.build();
    }

    @Nullable
    private static Notification.Builder geNotificationBuilder(
            final Context context, final String channelId, final int priority, final CharSequence title,
            final CharSequence notificationText, final CharSequence notificationBigText,
            final PendingIntent contentIntent, final PendingIntent deleteIntent) {
        if (context == null) return null;
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle(title);
        builder.setContentText(notificationText);
        builder.setStyle(new Notification.BigTextStyle().bigText(notificationBigText));
        builder.setContentIntent(contentIntent);
        builder.setDeleteIntent(deleteIntent);

        builder.setPriority(priority);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            builder.setChannelId(channelId);

        return builder;
    }

    private void setupNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        setupNotificationChannel(this, "springboot_notification_channel",
                "HTTP Server", NotificationManager.IMPORTANCE_LOW);
    }

    private static void setupNotificationChannel(final Context context, final String channelId, final CharSequence channelName, final int importance) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);

        NotificationManager notificationManager = ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));
        if (notificationManager != null)
            notificationManager.createNotificationChannel(channel);
    }
}
