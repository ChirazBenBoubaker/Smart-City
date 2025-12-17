package com.example.smartcity.web;

import com.example.smartcity.metier.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/citoyen/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    /**
     * Soumet un feedback pour un incident
     */
    @PostMapping("/soumettre")
    public String soumettreFeedback(
            @RequestParam Long incidentId,
            @RequestParam Integer note,
            @RequestParam(required = false) String commentaire,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        String email = userDetails.getUsername();

        // Vérifier si le citoyen peut donner un feedback
        if (!feedbackService.peutDonnerFeedback(incidentId, email)) {
            redirectAttributes.addFlashAttribute("error",
                    "❌ Vous ne pouvez pas donner de feedback pour cet incident.");
            return "redirect:/citoyen/mes-incidents/" + incidentId;
        }

        // Soumettre le feedback
        boolean success = feedbackService.soumettreFeedback(incidentId, email, note, commentaire);

        if (success) {
            redirectAttributes.addFlashAttribute("success",
                    "✅ Merci pour votre feedback ! Votre avis nous aide à améliorer nos services.");
        } else {
            redirectAttributes.addFlashAttribute("error",
                    "❌ Une erreur est survenue lors de l'envoi de votre feedback.");
        }

        return "redirect:/citoyen/mes-incidents/" + incidentId;
    }
}