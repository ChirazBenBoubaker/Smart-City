package com.example.smartcity.web;

import com.example.smartcity.model.entity.Incident;
import com.example.smartcity.model.enums.Departement;
import com.example.smartcity.model.enums.PrioriteIncident;
import com.example.smartcity.model.enums.StatutIncident;
import com.example.smartcity.metier.service.IncidentService;
import com.example.smartcity.metier.service.FeedbackService;
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
    private final FeedbackService feedbackService;

    /**
     * Liste des incidents du citoyen avec filtres complets
     */
    @GetMapping
    public String mesIncidents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dateSignalement") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String categorie,
            @RequestParam(required = false) String priorite,
            @RequestParam(required = false) String quartier,
            @RequestParam(required = false) String ville,
            @RequestParam(required = false) String gouvernorat,
            @RequestParam(required = false) String recherche,
            Principal principal,
            Model model
    ) {
        String email = principal.getName();

        // Convertir les paramètres String en Enum de manière sécurisée
        StatutIncident statutEnum = convertirStatut(statut);
        Departement categorieEnum = convertirCategorie(categorie);
        PrioriteIncident prioriteEnum = convertirPriorite(priorite);

        // Liste paginée des incidents avec filtres
        Page<Incident> incidents = incidentService.getIncidentsByCitoyenWithFilters(
                email, page, size, sortBy, direction,
                statutEnum, categorieEnum, prioriteEnum, quartier, ville, gouvernorat, recherche
        );

        // Statistiques
        long totalIncidents = incidentService.countTotal(email);
        long signales = incidentService.countByStatut(email, StatutIncident.SIGNALE);
        long enCours = incidentService.countByStatut(email, StatutIncident.EN_RESOLUTION);
        long resolus = incidentService.countByStatut(email, StatutIncident.RESOLU);

        // Liste des quartiers pour le filtre
        var quartiers = incidentService.getQuartiersForCitoyen(email);

        // Modèle
        model.addAttribute("incidents", incidents);
        model.addAttribute("totalIncidents", totalIncidents);
        model.addAttribute("signales", signales);
        model.addAttribute("enCours", enCours);
        model.addAttribute("resolus", resolus);

        // Options de filtres
        model.addAttribute("statuts", StatutIncident.values());
        model.addAttribute("categories", Departement.values());
        model.addAttribute("priorites", PrioriteIncident.values());
        model.addAttribute("quartiers", quartiers);

        // Valeurs actuelles des filtres
        model.addAttribute("currentStatut", statut);
        model.addAttribute("currentCategorie", categorie);
        model.addAttribute("currentPriorite", priorite);
        model.addAttribute("currentQuartier", quartier);
        model.addAttribute("currentRecherche", recherche != null ? recherche : "");

        model.addAttribute("villes", incidentService.getVillesForCitoyen(email));
        model.addAttribute("gouvernorats", incidentService.getGouvernoratsByCitoyen(email));

        model.addAttribute("currentVille", ville);
        model.addAttribute("currentGouvernorat", gouvernorat);

        // Pagination
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);

        // ✅ AJOUT : Service pour vérifier si feedback existe
        model.addAttribute("feedbackService", feedbackService);

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

                    // ✅ Vérifier si le citoyen peut donner un feedback
                    boolean peutDonnerFeedback = feedbackService.peutDonnerFeedback(id, email);
                    model.addAttribute("peutDonnerFeedback", peutDonnerFeedback);

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

    // ===== Méthodes de conversion String -> Enum =====

    private StatutIncident convertirStatut(String statut) {
        if (statut == null || statut.trim().isEmpty()) {
            return null;
        }
        try {
            return StatutIncident.valueOf(statut.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Departement convertirCategorie(String categorie) {
        if (categorie == null || categorie.trim().isEmpty()) {
            return null;
        }
        try {
            return Departement.valueOf(categorie.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private PrioriteIncident convertirPriorite(String priorite) {
        if (priorite == null || priorite.trim().isEmpty()) {
            return null;
        }
        try {
            return PrioriteIncident.valueOf(priorite.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}