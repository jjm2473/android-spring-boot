package com.example.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;


import com.example.simple_web.Application;

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
    public void onDestroy() {
        super.onDestroy();
    }
}
