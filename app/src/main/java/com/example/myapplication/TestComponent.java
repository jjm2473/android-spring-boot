package com.example.myapplication;

import org.springframework.stereotype.Component;

@Component
public class TestComponent {
    public String hello(String name) {
        return "hello "+name;
    }
}
