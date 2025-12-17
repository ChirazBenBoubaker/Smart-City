package com.example.smartcity.dto;

import java.util.List;

public record AdminReportData(
        long totalCitoyens,
        long totalAgents,
        long totalIncidents,
        long incidentsSignales,
        long incidentsPrisEnCharge,
        long incidentsResolus,
        long incidentsClotures,
        List<AgentStatsDTO> agents,

        List<IncidentByDepartementDTO> incidentsParDepartement

) {}
