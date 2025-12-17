package com.example.smartcity.dto;


import com.example.smartcity.model.enums.Departement;

public record AgentStatsDTO(
        String nom,
        String prenom,
        Departement departement,
        long totalIncidents,
        long incidentsTraites
) {}
