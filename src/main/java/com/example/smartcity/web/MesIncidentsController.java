package com.example.smartcity.web;

import com.example.smartcity.model.entity.Incident;
import com.example.smartcity.model.enums.StatutIncident;
import com.example.smartcity.metier.service.IncidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/citoyen/mes-incidents")
@RequiredArgsConstructor
public class MesIncidentsController {

    private final IncidentService incidentService;

    /**
     * Liste des incidents du citoyen (pagination + tri uniquement)
     */
    @GetMapping
    public String mesIncidents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dateSignalement") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            Principal principal,
            Model model
    ) {
        String email = principal.getName();

        // Liste paginée des incidents
        Page<Incident> incidents = incidentService.getIncidentsByCitoyen(
                email, page, size, sortBy, direction
        );

        // Statistiques
        long totalIncidents = incidentService.countTotal(email);
        long signales = incidentService.countByStatut(email, StatutIncident.SIGNALE);
        long enCours = incidentService.countByStatut(email, StatutIncident.EN_RESOLUTION);
        long resolus = incidentService.countByStatut(email, StatutIncident.RESOLU);

        // Modèle
        model.addAttribute("incidents", incidents);

        model.addAttribute("totalIncidents", totalIncidents);
        model.addAttribute("signales", signales);
        model.addAttribute("enCours", enCours);
        model.addAttribute("resolus", resolus);

        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);

        return "citoyen/mes-incidents";
    }

    /**
     * Détail d'un incident
     */
    @GetMapping("/{id}")
    public String detailIncident(
            @PathVariable Long id,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        String email = principal.getName();

        return incidentService.getIncidentById(id, email)
                .map(incident -> {
                    model.addAttribute("incident", incident);
                    return "citoyen/detail-incident";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute(
                            "error",
                            "Incident non trouvé ou accès non autorisé"
                    );
                    return "redirect:/citoyen/mes-incidents";
                });
    }

    /**
     * Suppression d'un incident
     */
    @PostMapping("/{id}/supprimer")
    public String supprimerIncident(
            @PathVariable Long id,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        String email = principal.getName();

        if (incidentService.deleteIncident(id, email)) {
            redirectAttributes.addFlashAttribute(
                    "success",
                    "Incident supprimé avec succès"
            );
        } else {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Impossible de supprimer cet incident"
            );
        }

        return "redirect:/citoyen/mes-incidents";
    }
}
