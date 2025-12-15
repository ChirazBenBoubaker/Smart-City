package com.example.smartcity.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/citoyen")
public class CitoyenController {

    @GetMapping("/dashboard")
    public String dashboard() {
        return "citoyen/dashboard";
    }
}
