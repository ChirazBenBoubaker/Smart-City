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

    @GetMapping("/declarer-incident")
    public String declarerIncident() {
        return "citoyen/declarer-incident";
    }

//    @GetMapping("/mes-incidents")
//    public String mesIncidents() {
//        return "citoyen/mes-incidents";
//    }

    @GetMapping("/statistiques")
    public String statistiques() {
        return "citoyen/statistiques";
    }
}