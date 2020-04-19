package com.example.myapplication;

import android.os.Build;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Hello {
    @Autowired
    private HelloComponent helloComponent;

    @RequestMapping("/hello")
    public String hello() {
        return helloComponent.hello(Build.BRAND) + " Greetings from Spring Boot!";
    }

}