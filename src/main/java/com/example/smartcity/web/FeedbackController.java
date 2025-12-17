package com.example.smartcity.web;

import com.example.smartcity.metier.service.FeedbackService;
import com.example.smartcity.metier.service.IncidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/citoyen/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final IncidentService incidentService;

    /**
     * Affiche la page de feedback pour un incident
     */
    @GetMapping("/{id}")
    public String afficherPageFeedback(
            @PathVariable Long id,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes) {

        String email = principal.getName();

        // Vérifier si le citoyen peut donner un feedback
        if (!feedbackService.peutDonnerFeedback(id, email)) {
            redirectAttributes.addFlashAttribute("error",
                    "❌ Vous ne pouvez pas donner de feedback pour cet incident.");
            return "redirect:/citoyen/mes-incidents";
        }

        // Récupérer l'incident
        return incidentService.getIncidentById(id, email)
                .map(incident -> {
                    model.addAttribute("incident", incident);
                    return "citoyen/feedback-page";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error",
                            "Incident non trouvé ou accès non autorisé");
                    return "redirect:/citoyen/mes-incidents";
                });
    }

    /**
     * Soumet un feedback pour un incident
     */
    @PostMapping
    public String soumettreFeedback(
            @RequestParam Long incidentId,
            @RequestParam Integer note,
            @RequestParam(required = false) String commentaire,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        String email = userDetails.getUsername();

        // Soumettre le feedback
        boolean success = feedbackService.soumettreFeedback(incidentId, email, note, commentaire);

        if (success) {
            redirectAttributes.addFlashAttribute("success",
                    "✅ Merci pour votre feedback ! Votre avis nous aide à améliorer nos services.");
        } else {
            redirectAttributes.addFlashAttribute("error",
                    "❌ Une erreur est survenue lors de l'envoi de votre feedback.");
        }

        return "redirect:/citoyen/mes-incidents";
    }
}