package com.example.smartcity.metier.service;

import com.example.smartcity.dao.AgentMunicipalRepository;
import com.example.smartcity.dao.IncidentRepository;
import com.example.smartcity.model.entity.AgentMunicipal;
import com.example.smartcity.model.entity.Incident;
import com.example.smartcity.model.enums.PrioriteIncident;
import com.example.smartcity.model.enums.StatutIncident;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IncidentAgentService {

    private final IncidentRepository incidentRepository;
    private final AgentMunicipalRepository agentRepository;
    private final ResolutionMailService emailService;

    /**
     * Compte le nombre total d'incidents assignés à l'agent
     */
    public long countIncidentsAssignes(String email) {
        AgentMunicipal agent = getAgent(email);
        return incidentRepository.countByAgentResponsable(agent);
    }

    /**
     * Compte les incidents par statut pour un agent
     */
    public long countByStatut(String email, StatutIncident statut) {
        AgentMunicipal agent = getAgent(email);
        return incidentRepository.countByAgentResponsableAndStatut(agent, statut);
    }

    /**
     * Compte les incidents par priorité pour un agent
     */
    public long countByPriorite(String email, PrioriteIncident priorite) {
        AgentMunicipal agent = getAgent(email);
        return incidentRepository.countByAgentResponsableAndPriorite(agent, priorite);
    }

    /**
     * Récupère les N derniers incidents de l'agent
     */
    public List<Incident> getRecentIncidents(String email, int limit) {
        AgentMunicipal agent = getAgent(email);
        Pageable pageable = PageRequest.of(0, limit, Sort.by("dateSignalement").descending());
        return incidentRepository.findByAgentResponsable(agent, pageable).getContent();
    }

    /**
     * Récupère les incidents avec filtres et pagination (AVEC LOCALISATION)
     */
    public Page<Incident> getIncidentsWithFilters(
            String email,
            int page,
            int size,
            String sortBy,
            String direction,
            StatutIncident statut,
            PrioriteIncident priorite,
            String quartier,
            String ville,
            String gouvernorat,
            String recherche) {

        AgentMunicipal agent = getAgent(email);
        Pageable pageable = buildPageable(page, size, sortBy, direction);

        return incidentRepository.findByAgentResponsableWithFiltersAvances(
                agent,
                statut,
                priorite,
                clean(quartier),
                clean(ville),
                clean(gouvernorat),
                clean(recherche),
                pageable
        );
    }

    /**
     * Récupère les villes distinctes des incidents de l'agent
     */
    public List<String> getVillesDistinctes(String email) {
        AgentMunicipal agent = getAgent(email);
        return incidentRepository.findDistinctVillesByAgent(agent);
    }

    /**
     * Récupère les gouvernorats distincts des incidents de l'agent
     */
    public List<String> getGouvernoratsDistincts(String email) {
        AgentMunicipal agent = getAgent(email);
        return incidentRepository.findDistinctGouvernoratsByAgent(agent);
    }

    /**
     * Récupère les quartiers distincts des incidents de l'agent
     */
    public List<String> getQuartiersDistincts(String email) {
        AgentMunicipal agent = getAgent(email);
        return incidentRepository.findDistinctQuartierNomsByAgent(agent);
    }

    /**
     * Prendre en charge un incident (PRIS_EN_CHARGE -> EN_RESOLUTION)
     */
    @Transactional
    public boolean prendreEnCharge(Long id, String email) {
        return incidentRepository.findById(id)
                .filter(i -> i.getAgentResponsable() != null)
                .filter(i -> i.getAgentResponsable().getEmail().equals(email))
                .filter(i -> i.getStatut() == StatutIncident.PRIS_EN_CHARGE)
                .map(i -> {
                    i.setStatut(StatutIncident.EN_RESOLUTION);
                    i.setDatePriseEnCharge(LocalDateTime.now());
                    incidentRepository.save(i);
                    return true;
                })
                .orElse(false);
    }

    /**
     * Marquer un incident comme résolu (EN_RESOLUTION -> RESOLU)
     */
    @Transactional
    public boolean marquerResolu(Long id, String email) {
        return incidentRepository.findById(id)
                .filter(i -> i.getAgentResponsable() != null)
                .filter(i -> i.getAgentResponsable().getEmail().equals(email))
                .filter(i -> i.getStatut() == StatutIncident.EN_RESOLUTION)
                .map(i -> {
                    i.setStatut(StatutIncident.RESOLU);
                    i.setDateResolution(LocalDateTime.now());

                    // Incrémenter le compteur de l'agent
                    AgentMunicipal agent = i.getAgentResponsable();
                    agent.setNombreIncidentsTraites(agent.getNombreIncidentsTraites() + 1);
                    agentRepository.save(agent);

                    incidentRepository.save(i);

                    // Envoyer les emails de notification
                    emailService.notifierResolutionIncident(i);

                    return true;
                })
                .orElse(false);
    }

    /**
     * Récupère les détails d'un incident pour un agent
     */
    public Incident getIncidentDetailsForAgent(Long id, String email) {
        AgentMunicipal agent = getAgent(email);

        return incidentRepository.findById(id)
                .filter(i -> i.getAgentResponsable() != null)
                .filter(i -> i.getAgentResponsable().getId().equals(agent.getId()))
                .orElseThrow(() ->
                        new RuntimeException("Accès refusé ou incident introuvable"));
    }

    // ===== MÉTHODES UTILITAIRES PRIVÉES =====

    /**
     * Récupère un agent par son email
     */
    private AgentMunicipal getAgent(String email) {
        return agentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Agent non trouvé avec l'email: " + email));
    }

    /**
     * Construit un objet Pageable pour la pagination
     */
    private Pageable buildPageable(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        return PageRequest.of(page, size, sort);
    }

    /**
     * Nettoie une chaîne de caractères (trim et null si vide)
     */
    private String clean(String value) {
        return (value != null && !value.trim().isEmpty()) ? value.trim() : null;
    }
}