package com.example.simple_web;

import org.springframework.stereotype.Component;

@Component
public class HelloComponent {
    public String hello(String name) {
        return "Hello " + name + "!";
    }
}
