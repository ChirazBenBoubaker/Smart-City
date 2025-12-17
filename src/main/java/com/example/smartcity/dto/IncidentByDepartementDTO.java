package com.example.smartcity.dto;



import com.example.smartcity.model.enums.Departement;

public record IncidentByDepartementDTO(
        Departement departement,
        long total
) {}
