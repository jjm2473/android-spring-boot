package com.example.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.springframework.boot.logging.LoggingSystem;

import javax.management.MBeanServerFactory;

public class MyService extends Service {
    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        Log.d("MyService", "start spring boot");
        System.setProperty("user.home", this.getApplicationContext().getFilesDir().getAbsolutePath());
        // disable logger
        System.setProperty(LoggingSystem.SYSTEM_PROPERTY, LoggingSystem.NONE);
        // for org.apache.tomcat.util.modeler.Registry.getMBeanServer
        MBeanServerFactory.createMBeanServer();
        DemoSpringBootApplication.main(new String[0]);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
