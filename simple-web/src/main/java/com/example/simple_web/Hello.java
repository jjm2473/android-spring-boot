package com.example.simple_web;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Hello {
    @Autowired
    private HelloComponent helloComponent;

    @RequestMapping("/hello")
    public String hello() {
        return helloComponent.hello(System.getProperty("http.agent")) + " Greetings from Spring Boot!";
    }

}