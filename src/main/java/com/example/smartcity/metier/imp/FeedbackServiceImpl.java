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

@Service
@RequiredArgsConstructor
@Transactional
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

        Incident incident = incidentRepository.findById(incidentId).orElse(null);
        if (incident == null) return false;

        if (incident.getStatut() != StatutIncident.RESOLU) return false;

        if (incident.getCitoyen() == null ||
                !incident.getCitoyen().getEmail().equals(emailCitoyen)) {
            return false;
        }

        return !feedbackRepository.existsByIncidentId(incidentId);
    }

    /**
     * Soumission du feedback
     */
    @Override
    public boolean soumettreFeedback(Long incidentId, String emailCitoyen,
                                     Integer note, String commentaire) {

        if (note == null || note < 1 || note > 5) {
            return false;
        }

        if (!peutDonnerFeedback(incidentId, emailCitoyen)) {
            return false;
        }

        Incident incident = incidentRepository.findById(incidentId).orElse(null);
        Citoyen citoyen = citoyenRepository.findByEmail(emailCitoyen).orElse(null);

        if (incident == null || citoyen == null) {
            return false;
        }

        Feedback feedback = new Feedback();
        feedback.setIncident(incident);
        feedback.setCitoyen(citoyen);
        feedback.setNote(note);
        feedback.setCommentaire(commentaire);

        feedbackRepository.save(feedback);
        return true;
    }
}
