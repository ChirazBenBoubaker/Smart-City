package com.example.smartcity.dao;

import com.example.smartcity.model.entity.Citoyen;
import com.example.smartcity.model.entity.Incident;
import com.example.smartcity.model.enums.StatutIncident;
import com.example.smartcity.model.enums.Departement;
import com.example.smartcity.model.enums.PrioriteIncident;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    // Trouver tous les incidents d'un citoyen
    Page<Incident> findByCitoyen(Citoyen citoyen, Pageable pageable);

    // Compter les incidents par statut pour un citoyen
    long countByCitoyenAndStatut(Citoyen citoyen, StatutIncident statut);

    // Compter le total des incidents d'un citoyen
    long countByCitoyen(Citoyen citoyen);

    List<Incident> findTop5ByCitoyenOrderByDateSignalementDesc(Citoyen citoyen);

}