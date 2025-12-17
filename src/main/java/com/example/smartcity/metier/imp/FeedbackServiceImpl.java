package com.example.smartcity.metier.imp;

import com.example.smartcity.dao.CitoyenRepository;
import com.example.smartcity.dao.FeedbackRepository;
import com.example.smartcity.dao.IncidentRepository;
import com.example.smartcity.metier.service.FeedbackService;
import com.example.smartcity.model.entity.Citoyen;
import com.example.smartcity.model.entity.Feedback;
import com.example.smartcity.model.entity.Incident;
import com.example.smartcity.model.enums.StatutIncident;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final IncidentRepository incidentRepository;
    private final CitoyenRepository citoyenRepository;

    /**
     * Conditions pour donner un feedback :
     * 1️⃣ Incident existe
     * 2️⃣ Incident appartient au citoyen
     * 3️⃣ Incident est RESOLU
     * 4️⃣ Aucun feedback n'existe déjà
     */
    @Override
    public boolean peutDonnerFeedback(Long incidentId, String emailCitoyen) {
        return incidentRepository.findById(incidentId)
                .filter(i -> i.getCitoyen() != null)
                .filter(i -> i.getCitoyen().getEmail().equals(emailCitoyen))
                .filter(i -> i.getStatut() == StatutIncident.RESOLU)
                .filter(i -> !feedbackRepository.existsByIncidentId(incidentId))
                .isPresent();
    }

    /**
     * Vérifie si un feedback existe pour cet incident
     */
    @Override
    public boolean feedbackExiste(Long incidentId) {
        return feedbackRepository.existsByIncidentId(incidentId);
    }

    /**
     * Soumission du feedback
     */
    @Override
    @Transactional
    public boolean soumettreFeedback(Long incidentId, String emailCitoyen,
                                     Integer note, String commentaire) {

        // Validation de la note
        if (note == null || note < 1 || note > 5) {
            return false;
        }

        // Vérifier si le feedback peut être donné
        if (!peutDonnerFeedback(incidentId, emailCitoyen)) {
            return false;
        }

        // Récupération des entités
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new RuntimeException("Incident non trouvé"));

        Citoyen citoyen = citoyenRepository.findByEmail(emailCitoyen)
                .orElseThrow(() -> new RuntimeException("Citoyen non trouvé"));

        // Création du feedback
        Feedback feedback = new Feedback();
        feedback.setIncident(incident);
        feedback.setCitoyen(citoyen);
        feedback.setNote(note);
        feedback.setCommentaire(commentaire);
        feedback.setDateCreation(LocalDateTime.now());

        feedbackRepository.save(feedback);

        // 🖨️ LOG CONSOLE
        System.out.println("===== FEEDBACK ENREGISTRÉ =====");
        System.out.println("ID Incident : " + incidentId);
        System.out.println("Citoyen : " + citoyen.getPrenom() + " " + citoyen.getNom());
        System.out.println("Note : " + note + "/5 ⭐");
        System.out.println("Commentaire : " + (commentaire != null ? commentaire : "(aucun)"));
        System.out.println("Date : " + LocalDateTime.now());
        System.out.println("===============================");

        return true;
    }
}