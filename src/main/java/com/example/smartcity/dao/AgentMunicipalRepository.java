package com.example.smartcity.dao;

import com.example.smartcity.model.entity.AgentMunicipal;
import com.example.smartcity.model.enums.Departement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgentMunicipalRepository extends JpaRepository<AgentMunicipal, Long> {

    // Méthodes existantes
    List<AgentMunicipal> findByDepartement(Departement departement);
    List<AgentMunicipal> findByEnServiceTrue();
    Page<AgentMunicipal> findByEnabledTrue(PageRequest pageRequest);
    List<AgentMunicipal> findByDepartementAndEnabledTrueAndEnServiceTrue(Departement departement);
    long countByEnabledTrue();
    boolean existsByEmail(String email);
    long count();

}

    // ===== NOUVELLE MÉTHODE À AJOUTER =====
    /**
     * Trouve un agent par son email
     * Utilisé pour l'authentification et les services de l'agent
     */
    Optional<AgentMunicipal> findByEmail(String email);
}
