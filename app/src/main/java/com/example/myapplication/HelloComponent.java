package com.example.myapplication;

import org.springframework.stereotype.Component;

@Component
public class HelloComponent {
    public String hello(String name) {
        return "Hello " + name + "!";
    }
}
