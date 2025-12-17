package com.example.smartcity.metier.service;

import com.example.smartcity.dao.CitoyenRepository;
import com.example.smartcity.dao.IncidentRepository;
import com.example.smartcity.model.entity.Citoyen;
import com.example.smartcity.model.entity.Incident;
import com.example.smartcity.model.enums.Departement;
import com.example.smartcity.model.enums.PrioriteIncident;
import com.example.smartcity.model.enums.StatutIncident;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final CitoyenRepository citoyenRepository;

    // ===== BASE =====

    public Page<Incident> getIncidentsByCitoyen(
            String email, int page, int size, String sortBy, String direction) {

        Citoyen citoyen = getCitoyen(email);
        Pageable pageable = buildPageable(page, size, sortBy, direction);

        return incidentRepository.findByCitoyen(citoyen, pageable);
    }

    public Optional<Incident> getIncidentById(Long id, String email) {
        return incidentRepository.findById(id)
                .filter(i -> i.getCitoyen().getEmail().equals(email));
    }

    public long countByStatut(String email, StatutIncident statut) {
        return incidentRepository.countByCitoyenAndStatut(getCitoyen(email), statut);
    }

    public long countTotal(String email) {
        return incidentRepository.countByCitoyen(getCitoyen(email));
    }

    public List<Incident> getRecentIncidents(String email) {
        return incidentRepository.findTop5ByCitoyenOrderByDateSignalementDesc(getCitoyen(email));
    }

    // ===== SUPPRESSION =====

    @Transactional
    public boolean deleteIncident(Long id, String email) {
        return getIncidentById(id, email)
                .filter(i -> i.getStatut() == StatutIncident.SIGNALE)
                .map(i -> {
                    incidentRepository.delete(i);
                    return true;
                })
                .orElse(false);
    }

    // ===== FILTRAGE AVANCÉ =====

    public Page<Incident> getIncidentsByCitoyenWithFilters(
            String email,
            int page,
            int size,
            String sortBy,
            String direction,
            StatutIncident statut,
            Departement categorie,
            PrioriteIncident priorite,
            String quartier,
            String ville,
            String gouvernorat,
            String recherche
    ) {
        Citoyen citoyen = getCitoyen(email);
        Pageable pageable = buildPageable(page, size, sortBy, direction);

        return incidentRepository.findByCitoyenWithFiltersAvances(
                citoyen,
                statut,
                categorie,
                priorite,
                clean(quartier),
                clean(ville),
                clean(gouvernorat),
                clean(recherche),
                pageable
        );
    }

    // ===== LISTES POUR FILTRES =====

    public List<String> getGouvernoratsByCitoyen(String email) {
        return incidentRepository.findDistinctGouvernoratsByCitoyen(getCitoyen(email));
    }

    public List<String> getVillesForCitoyen(String email) {
        return incidentRepository.findDistinctVillesByCitoyen(getCitoyen(email));
    }

    public List<String> getQuartiersForCitoyen(String email) {
        return incidentRepository.findDistinctQuartierNomsByCitoyen(getCitoyen(email));
    }

    // ===== UTILITAIRES =====

    private Citoyen getCitoyen(String email) {
        return citoyenRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Citoyen non trouvé"));
    }

    @Transactional
    public void supprimerParAdmin(Long id) {

        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident introuvable"));



        incidentRepository.delete(incident);
    }




    private Pageable buildPageable(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        return PageRequest.of(page, size, sort);
    }

    private String clean(String value) {
        return (value != null && !value.trim().isEmpty()) ? value.trim() : null;
    }
    public List<Incident> getTop3RecentIncidents(String email) {
        Citoyen citoyen = getCitoyen(email);
        Pageable pageable = PageRequest.of(0, 3, Sort.by("dateSignalement").descending());
        Page<Incident> page = incidentRepository.findByCitoyen(citoyen, pageable);
        return page.getContent();
    }


}