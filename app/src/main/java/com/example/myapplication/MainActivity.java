package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.startService(new Intent(this, MyService.class));
//        new Thread() {
//            @Override
//            public void run() {
//                new MyService(){
//                    @Override
//                    public Context getApplicationContext() {
//                        return MainActivity.this.getApplicationContext();
//                    }
//                }.onCreate();
//            }
//        }.start();
    }
}
