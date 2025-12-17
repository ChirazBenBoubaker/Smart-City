package com.example.smartcity.metier.service;

public interface FeedbackService {

    /**
     * Vérifie si un citoyen peut donner un feedback sur un incident
     */
    boolean peutDonnerFeedback(Long incidentId, String emailCitoyen);

    /**
     * Soumettre un feedback
     */
    boolean soumettreFeedback(Long incidentId, String emailCitoyen,
                              Integer note, String commentaire);
}
