package com.example.smartcity.web;

import com.example.smartcity.metier.service.IncidentService;
import com.example.smartcity.model.enums.StatutIncident;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/citoyen")
@RequiredArgsConstructor
public class CitoyenController {

    private final IncidentService incidentService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {

        // Email de l'utilisateur connecté
        String email = authentication.getName();

        // ===== STATISTIQUES =====
        long totalIncidents = incidentService.countTotal(email);
        long signales = incidentService.countByStatut(email, StatutIncident.SIGNALE);
        long enCours = incidentService.countByStatut(email, StatutIncident.EN_RESOLUTION);
        long resolus = incidentService.countByStatut(email, StatutIncident.RESOLU);

        // ===== 3 DERNIERS INCIDENTS =====
        var incidents = incidentService.getTop3RecentIncidents(email);

        // ===== AJOUT AU MODEL =====
        model.addAttribute("totalIncidents", totalIncidents);
        model.addAttribute("signales", signales);
        model.addAttribute("enCours", enCours);
        model.addAttribute("resolus", resolus);
        model.addAttribute("incidents", incidents);

        return "citoyen/dashboard";
    }

    @GetMapping("/declarer-incident")
    public String declarerIncident() {
        return "citoyen/declarer-incident";
    }
}
