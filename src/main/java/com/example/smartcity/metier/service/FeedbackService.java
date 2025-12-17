package com.example.smartcity.metier.service;

public interface FeedbackService {

    /**
     * Vérifie si le citoyen peut donner un feedback pour cet incident
     * @param incidentId ID de l'incident
     * @param email Email du citoyen
     * @return true si le feedback est possible
     */
    boolean peutDonnerFeedback(Long incidentId, String email);

    /**
     * Vérifie si un feedback existe déjà pour cet incident
     * @param incidentId ID de l'incident
     * @return true si un feedback existe
     */
    boolean feedbackExiste(Long incidentId);

    /**
     * Soumet un feedback pour un incident
     * @param incidentId ID de l'incident
     * @param email Email du citoyen
     * @param note Note de 1 à 5
     * @param commentaire Commentaire optionnel
     * @return true si la soumission a réussi
     */
    boolean soumettreFeedback(Long incidentId, String email, Integer note, String commentaire);
}