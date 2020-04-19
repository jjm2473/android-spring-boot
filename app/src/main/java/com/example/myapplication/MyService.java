package com.example.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.context.annotation.AndroidConfigurationClassPostProcessor;

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
        // disable logger
        System.setProperty(LoggingSystem.SYSTEM_PROPERTY, LoggingSystem.NONE);
        // for org.apache.tomcat.util.modeler.Registry.getMBeanServer
        MBeanServerFactory.createMBeanServer();
        new SpringApplicationBuilder()
                .sources(AndroidConfigurationClassPostProcessor.class, DemoSpringBootApplication.class)
                .run();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
