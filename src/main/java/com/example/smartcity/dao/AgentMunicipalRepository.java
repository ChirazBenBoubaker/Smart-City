package com.example.smartcity.dao;

import com.example.smartcity.model.entity.AgentMunicipal;
import com.example.smartcity.model.enums.Departement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgentMunicipalRepository extends JpaRepository<AgentMunicipal, Long> {
    List<AgentMunicipal> findByDepartement(Departement departement);
    List<AgentMunicipal> findByEnServiceTrue();
}
