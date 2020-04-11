package com.example.myapplication;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Hello {

    @RequestMapping("/test")
    public String index() {
        return "Greetings from Spring Boot!";
    }

}