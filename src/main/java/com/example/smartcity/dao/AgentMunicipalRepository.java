package com.example.smartcity.dao;

import com.example.smartcity.model.entity.AgentMunicipal;
import com.example.smartcity.model.enums.Departement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgentMunicipalRepository extends JpaRepository<AgentMunicipal, Long> {
    List<AgentMunicipal> findByDepartement(Departement departement);
    List<AgentMunicipal> findByEnServiceTrue();
    Page<AgentMunicipal> findByEnabledTrue(PageRequest pageRequest);
    List<AgentMunicipal> findByDepartementAndEnabledTrueAndEnServiceTrue(Departement departement);
    long countByEnabledTrue();
    boolean existsByEmail(String email);
}
