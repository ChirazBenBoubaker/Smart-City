package com.example.smartcity.metier.service;

import com.example.smartcity.dao.AgentMunicipalRepository;
import com.example.smartcity.dao.CitoyenRepository;
import com.example.smartcity.dao.IncidentRepository;
import com.example.smartcity.dto.AdminReportData;
import com.example.smartcity.dto.AgentStatsDTO;
import com.example.smartcity.dto.IncidentByDepartementDTO;
import com.example.smartcity.model.entity.AgentMunicipal;
import com.example.smartcity.model.enums.Departement;
import com.example.smartcity.model.enums.StatutIncident;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminReportService {

    private final AgentMunicipalRepository agentRepo;
    private final CitoyenRepository citoyenRepo;
    private final IncidentRepository incidentRepo;

    public AdminReportData buildReport() {

        // ===== TOTAUX =====
        long totalCitoyens = citoyenRepo.count();
        long totalAgents = agentRepo.count();
        long totalIncidents = incidentRepo.count();

        long signales = incidentRepo.countByStatut(StatutIncident.SIGNALE);
        long prisEnCharge = incidentRepo.countByStatut(StatutIncident.PRIS_EN_CHARGE);
        long resolus = incidentRepo.countByStatut(StatutIncident.RESOLU);
        long clotures = incidentRepo.countByStatut(StatutIncident.CLOTURE);

        // ===== PAR AGENT =====
        List<Object[]> totalByAgent = incidentRepo.countIncidentsByAgent();
        List<Object[]> resolvedByAgent = incidentRepo.countResolvedIncidentsByAgent();

        Map<AgentMunicipal, Long> resolvedMap = resolvedByAgent.stream()
                .collect(Collectors.toMap(
                        r -> (AgentMunicipal) r[0],
                        r -> (Long) r[1]
                ));

        List<AgentStatsDTO> agentStats = totalByAgent.stream()
                .map(row -> {
                    AgentMunicipal a = (AgentMunicipal) row[0];
                    long total = (Long) row[1];
                    long done = resolvedMap.getOrDefault(a, 0L);

                    return new AgentStatsDTO(
                            a.getNom(),
                            a.getPrenom(),
                            a.getDepartement(),
                            total,
                            done
                    );
                })
                .toList();

        // ===== PAR DÉPARTEMENT =====
        List<Object[]> depResults = incidentRepo.countIncidentsByDepartement();

        List<IncidentByDepartementDTO> incidentsParDepartement =
                depResults.stream()
                        .map(r -> new IncidentByDepartementDTO(
                                (Departement) r[0],
                                (Long) r[1]
                        ))
                        .toList();

        // ===== RAPPORT FINAL =====
        return new AdminReportData(
                totalCitoyens,
                totalAgents,
                totalIncidents,
                signales,
                prisEnCharge,
                resolus,
                clotures,
                agentStats,
                incidentsParDepartement
        );
    }
}
