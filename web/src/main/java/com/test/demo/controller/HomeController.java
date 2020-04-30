package com.test.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ryan on 2017/11/18/0018.
 */
@Controller
public class HomeController {

    @GetMapping("/home")
    public String index(Model model, @RequestParam("name") String name) {
        final Map<String, Object> user = new HashMap<>();
        user.put("name", name);

        model.addAttribute("user", user);
        model.addAttribute("msg", "Hello World!");
        return "home";
    }

    @GetMapping("/api")
    public String api(){
        return "redirect:/swagger-ui.html";
    }
}
