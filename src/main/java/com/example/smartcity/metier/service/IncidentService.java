package com.example.smartcity.metier.service;

import com.example.smartcity.model.entity.Citoyen;
import com.example.smartcity.model.entity.Incident;
import com.example.smartcity.model.enums.StatutIncident;
import com.example.smartcity.model.enums.Departement;
import com.example.smartcity.model.enums.PrioriteIncident;
import com.example.smartcity.dao.IncidentRepository;
import com.example.smartcity.dao.CitoyenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final CitoyenRepository citoyenRepository;

    /**
     * Récupère tous les incidents d'un citoyen avec pagination
     */
    public Page<Incident> getIncidentsByCitoyen(String email, int page, int size, String sortBy, String direction) {
        Citoyen citoyen = citoyenRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Citoyen non trouvé"));

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return incidentRepository.findByCitoyen(citoyen, pageable);
    }

    /**
     * Récupère un incident par son ID (seulement si appartient au citoyen)
     */
    public Optional<Incident> getIncidentById(Long id, String email) {
        return incidentRepository.findById(id)
                .filter(incident -> incident.getCitoyen().getEmail().equals(email));
    }

    /**
     * Compte le nombre d'incidents par statut pour un citoyen
     */
    public long countByStatut(String email, StatutIncident statut) {
        Citoyen citoyen = citoyenRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Citoyen non trouvé"));
        return incidentRepository.countByCitoyenAndStatut(citoyen, statut);
    }

    /**
     * Compte le total des incidents d'un citoyen
     */
    public long countTotal(String email) {
        Citoyen citoyen = citoyenRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Citoyen non trouvé"));
        return incidentRepository.countByCitoyen(citoyen);
    }

    /**
     * Récupère les 5 derniers incidents d'un citoyen
     */
    public List<Incident> getRecentIncidents(String email) {
        Citoyen citoyen = citoyenRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Citoyen non trouvé"));
        return incidentRepository.findTop5ByCitoyenOrderByDateSignalementDesc(citoyen);
    }

    /**
     * Supprime un incident (seulement si SIGNALE et appartient au citoyen)
     */
    @Transactional
    public boolean deleteIncident(Long id, String email) {
        Optional<Incident> incidentOpt = getIncidentById(id, email);

        if (incidentOpt.isPresent()) {
            Incident incident = incidentOpt.get();

            // On ne peut supprimer que les incidents non encore traités
            if (incident.getStatut() == StatutIncident.SIGNALE) {
                incidentRepository.delete(incident);
                return true;
            }
        }
        return false;
    }
}