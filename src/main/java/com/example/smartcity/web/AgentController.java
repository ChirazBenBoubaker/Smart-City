package com.example.smartcity.web;

import com.example.smartcity.metier.service.AgentService;
import com.example.smartcity.metier.service.IncidentAgentService;
import com.example.smartcity.model.entity.AgentMunicipal;
import com.example.smartcity.model.entity.Incident;
import com.example.smartcity.model.enums.PrioriteIncident;
import com.example.smartcity.model.enums.StatutIncident;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/agent")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;
    private final IncidentAgentService incidentAgentService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String email = userDetails.getUsername();

        // Statistiques
        model.addAttribute("totalAssignes", incidentAgentService.countIncidentsAssignes(email));
        model.addAttribute("enCours", incidentAgentService.countByStatut(email, StatutIncident.EN_RESOLUTION));
        model.addAttribute("resolus", incidentAgentService.countByStatut(email, StatutIncident.RESOLU));
        model.addAttribute("nouveaux", incidentAgentService.countByStatut(email, StatutIncident.PRIS_EN_CHARGE));

        // Incidents récents
        model.addAttribute("incidentsRecents", incidentAgentService.getRecentIncidents(email, 5));

        // Incidents par priorité
        model.addAttribute("urgents", incidentAgentService.countByPriorite(email, PrioriteIncident.URGENT));
        model.addAttribute("haute", incidentAgentService.countByPriorite(email, PrioriteIncident.ELEVEE));
        model.addAttribute("moyenne", incidentAgentService.countByPriorite(email, PrioriteIncident.MOYENNE));

        // Info agent
        model.addAttribute("agent", agentService.getAgentByEmail(email));

        return "agent/dashboard";
    }

    @GetMapping("/incidents")
    public String mesIncidents(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dateSignalement") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String priorite,
            @RequestParam(required = false) String quartier,
            @RequestParam(required = false) String ville,
            @RequestParam(required = false) String gouvernorat,
            @RequestParam(required = false) String recherche,
            Model model) {

        String email = userDetails.getUsername();

        // Conversion des paramètres String en enums
        StatutIncident statutEnum = null;
        if (statut != null && !statut.isEmpty()) {
            try {
                statutEnum = StatutIncident.valueOf(statut);
            } catch (IllegalArgumentException e) {
                // Valeur invalide, on ignore
            }
        }

        PrioriteIncident prioriteEnum = null;
        if (priorite != null && !priorite.isEmpty()) {
            try {
                prioriteEnum = PrioriteIncident.valueOf(priorite);
            } catch (IllegalArgumentException e) {
                // Valeur invalide, on ignore
            }
        }

        // Récupération des incidents avec tous les filtres
        Page<Incident> incidents = incidentAgentService.getIncidentsWithFilters(
                email, page, size, sortBy, direction,
                statutEnum, prioriteEnum,
                quartier, ville, gouvernorat,
                recherche
        );

        // Statistiques pour les cartes
        model.addAttribute("totalIncidents", incidentAgentService.countIncidentsAssignes(email));
        model.addAttribute("signales", incidentAgentService.countByStatut(email, StatutIncident.PRIS_EN_CHARGE));
        model.addAttribute("enCours", incidentAgentService.countByStatut(email, StatutIncident.EN_RESOLUTION));
        model.addAttribute("resolus", incidentAgentService.countByStatut(email, StatutIncident.RESOLU));

        // Données de pagination
        model.addAttribute("incidents", incidents);
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);

        // Filtres actuels
        model.addAttribute("currentStatut", statut);
        model.addAttribute("currentPriorite", priorite);
        model.addAttribute("currentQuartier", quartier);
        model.addAttribute("currentVille", ville);
        model.addAttribute("currentGouvernorat", gouvernorat);
        model.addAttribute("currentRecherche", recherche);

        // Listes pour les filtres
        model.addAttribute("statuts", StatutIncident.values());
        model.addAttribute("priorites", PrioriteIncident.values());

        // Listes de localisation (valeurs distinctes des incidents de l'agent)
        model.addAttribute("gouvernorats", incidentAgentService.getGouvernoratsDistincts(email));
        model.addAttribute("villes", incidentAgentService.getVillesDistinctes(email));
        model.addAttribute("quartiers", incidentAgentService.getQuartiersDistincts(email));

        return "agent/incidents";
    }

    @GetMapping("/incidents/{id}")
    public String detailsIncident(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        String email = userDetails.getUsername();

        try {
            Incident incident = incidentAgentService.getIncidentDetailsForAgent(id, email);
            model.addAttribute("incident", incident);
            return "agent/incident-details";
        } catch (RuntimeException e) {
            model.addAttribute("error", "Incident introuvable ou accès refusé");
            return "redirect:/agent/incidents";
        }
    }

    @PostMapping("/incidents/{id}/prendre-en-charge")
    public String prendreEnCharge(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        boolean success = incidentAgentService.prendreEnCharge(id, userDetails.getUsername());

        if (success) {
            redirectAttributes.addFlashAttribute("success",
                    "✅ Incident pris en charge avec succès ! Vous pouvez maintenant commencer la résolution.");
        } else {
            redirectAttributes.addFlashAttribute("error",
                    "❌ Impossible de prendre en charge cet incident. Vérifiez qu'il est bien au statut 'PRIS_EN_CHARGE'.");
        }

        return "redirect:/agent/incidents/" + id;
    }

    @PostMapping("/incidents/{id}/resoudre")
    public String resoudre(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        boolean success = incidentAgentService.marquerResolu(id, userDetails.getUsername());

        if (success) {
            redirectAttributes.addFlashAttribute("success",
                    "🎉 Incident résolu avec succès ! Excellent travail. L'incident a été marqué comme résolu.");
        } else {
            redirectAttributes.addFlashAttribute("error",
                    "❌ Impossible de résoudre cet incident. Vérifiez qu'il est bien au statut 'EN_RESOLUTION'.");
        }

        return "redirect:/agent/incidents/" + id;
    }



    @GetMapping("/profile")
    public String profile(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {



        String email = userDetails.getUsername();
        AgentMunicipal agent = agentService.getAgentByEmail(email);

        model.addAttribute("agent", agent);
        return "agent/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String prenom,
            @RequestParam String nom,
            @RequestParam(required = false) String telephone,
            RedirectAttributes redirectAttributes) {

        agentService.updateProfile(
                userDetails.getUsername(),
                prenom,
                nom,
                telephone
        );

        redirectAttributes.addFlashAttribute(
                "success", "✅ Profil mis à jour avec succès"
        );

        return "redirect:/agent/profile";
    }


    @PostMapping("/profile/change-password")
    public String changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {

        String email = userDetails.getUsername();

        boolean success = agentService.changePassword(
                email, currentPassword, newPassword, confirmPassword
        );

        if (success) {
            redirectAttributes.addFlashAttribute(
                    "success", "✅ Mot de passe modifié avec succès");
        } else {
            redirectAttributes.addFlashAttribute(
                    "error", "❌ Mot de passe actuel incorrect ou confirmation invalide");
        }

        return "redirect:/agent/profile";
    }

}