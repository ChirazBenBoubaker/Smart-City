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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

// ✅ Test unitaire avec Mockito uniquement (pas de Spring)
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests du service AdminReportService")
class AdminReportServiceTest {

    @Mock
    private AgentMunicipalRepository agentRepo;

    @Mock
    private CitoyenRepository citoyenRepo;

    @Mock
    private IncidentRepository incidentRepo;

    @InjectMocks
    private AdminReportService adminReportService;

    private AgentMunicipal agent1;
    private AgentMunicipal agent2;

    @BeforeEach
    void setUp() {
        agent1 = new AgentMunicipal();
        agent1.setId(1L);
        agent1.setNom("Dupont");
        agent1.setPrenom("Jean");
        agent1.setDepartement(Departement.VOIRIE);

        agent2 = new AgentMunicipal();
        agent2.setId(2L);
        agent2.setNom("Martin");
        agent2.setPrenom("Marie");
        agent2.setDepartement(Departement.SECURITE);
    }

    @Test
    @DisplayName("Doit générer un rapport complet avec toutes les statistiques")
    void devraitGenererRapportComplet() {
        // Given
        when(citoyenRepo.count()).thenReturn(150L);
        when(agentRepo.count()).thenReturn(25L);
        when(incidentRepo.count()).thenReturn(320L);

        when(incidentRepo.countByStatut(StatutIncident.SIGNALE)).thenReturn(45L);
        when(incidentRepo.countByStatut(StatutIncident.PRIS_EN_CHARGE)).thenReturn(120L);
        when(incidentRepo.countByStatut(StatutIncident.RESOLU)).thenReturn(100L);
        when(incidentRepo.countByStatut(StatutIncident.CLOTURE)).thenReturn(55L);

        List<Object[]> totalByAgent = Arrays.asList(
                new Object[]{agent1, 50L},
                new Object[]{agent2, 35L}
        );
        when(incidentRepo.countIncidentsByAgent()).thenReturn(totalByAgent);

        List<Object[]> resolvedByAgent = Arrays.asList(
                new Object[]{agent1, 30L},
                new Object[]{agent2, 20L}
        );
        when(incidentRepo.countResolvedIncidentsByAgent()).thenReturn(resolvedByAgent);

        List<Object[]> incidentsByDep = Arrays.asList(
                new Object[]{Departement.VOIRIE, 150L},
                new Object[]{Departement.SECURITE, 100L},
                new Object[]{Departement.PROPRETE, 70L}
        );
        when(incidentRepo.countIncidentsByDepartement()).thenReturn(incidentsByDep);

        // When
        AdminReportData report = adminReportService.buildReport();

        // Then
        assertThat(report).isNotNull();

        assertThat(report.totalCitoyens()).isEqualTo(150L);
        assertThat(report.totalAgents()).isEqualTo(25L);
        assertThat(report.totalIncidents()).isEqualTo(320L);

        assertThat(report.incidentsSignales()).isEqualTo(45L);
        assertThat(report.incidentsPrisEnCharge()).isEqualTo(120L);
        assertThat(report.incidentsResolus()).isEqualTo(100L);
        assertThat(report.incidentsClotures()).isEqualTo(55L);

        assertThat(report.agents()).hasSize(2);
        AgentStatsDTO agent1Stats = report.agents().get(0);
        assertThat(agent1Stats.nom()).isEqualTo("Dupont");
        assertThat(agent1Stats.prenom()).isEqualTo("Jean");
        assertThat(agent1Stats.departement()).isEqualTo(Departement.VOIRIE);
        assertThat(agent1Stats.totalIncidents()).isEqualTo(50L);
        assertThat(agent1Stats.incidentsTraites()).isEqualTo(30L);

        assertThat(report.incidentsParDepartement()).hasSize(3);
        IncidentByDepartementDTO voirieStats = report.incidentsParDepartement().get(0);
        assertThat(voirieStats.departement()).isEqualTo(Departement.VOIRIE);
        assertThat(voirieStats.total()).isEqualTo(150L);

        verify(citoyenRepo).count();
        verify(agentRepo).count();
        verify(incidentRepo).count();
        verify(incidentRepo).countByStatut(StatutIncident.SIGNALE);
        verify(incidentRepo).countByStatut(StatutIncident.PRIS_EN_CHARGE);
        verify(incidentRepo).countByStatut(StatutIncident.RESOLU);
        verify(incidentRepo).countByStatut(StatutIncident.CLOTURE);
        verify(incidentRepo).countIncidentsByAgent();
        verify(incidentRepo).countResolvedIncidentsByAgent();
        verify(incidentRepo).countIncidentsByDepartement();
    }

    @Test
    @DisplayName("Doit gérer le cas où aucun agent n'a d'incidents")
    void devraitGererAucunIncidentParAgent() {
        // Given
        when(citoyenRepo.count()).thenReturn(50L);
        when(agentRepo.count()).thenReturn(10L);
        when(incidentRepo.count()).thenReturn(0L);
        when(incidentRepo.countByStatut(any())).thenReturn(0L);
        when(incidentRepo.countIncidentsByAgent()).thenReturn(Collections.emptyList());
        when(incidentRepo.countResolvedIncidentsByAgent()).thenReturn(Collections.emptyList());
        when(incidentRepo.countIncidentsByDepartement()).thenReturn(Collections.emptyList());

        // When
        AdminReportData report = adminReportService.buildReport();

        // Then
        assertThat(report).isNotNull();
        assertThat(report.totalIncidents()).isZero();
        assertThat(report.agents()).isEmpty();
        assertThat(report.incidentsParDepartement()).isEmpty();
    }

    @Test
    @DisplayName("Doit gérer les agents sans incidents résolus")
    void devraitGererAgentsSansIncidentsResolus() {
        // Given
        when(citoyenRepo.count()).thenReturn(100L);
        when(agentRepo.count()).thenReturn(15L);
        when(incidentRepo.count()).thenReturn(50L);
        when(incidentRepo.countByStatut(any())).thenReturn(10L);

        List<Object[]> totalByAgent = Arrays.asList(
                new Object[]{agent1, 30L},
                new Object[]{agent2, 20L}
        );
        when(incidentRepo.countIncidentsByAgent()).thenReturn(totalByAgent);
        when(incidentRepo.countResolvedIncidentsByAgent()).thenReturn(Collections.emptyList());
        when(incidentRepo.countIncidentsByDepartement()).thenReturn(Collections.emptyList());

        // When
        AdminReportData report = adminReportService.buildReport();

        // Then
        assertThat(report.agents()).hasSize(2);
        assertThat(report.agents().get(0).incidentsTraites()).isZero();
        assertThat(report.agents().get(1).incidentsTraites()).isZero();
    }

    @Test
    @DisplayName("Doit calculer correctement les incidents traités par agent")
    void devraitCalculerIncidentsTraitesParAgent() {
        // Given
        when(citoyenRepo.count()).thenReturn(100L);
        when(agentRepo.count()).thenReturn(15L);
        when(incidentRepo.count()).thenReturn(85L);
        when(incidentRepo.countByStatut(any())).thenReturn(20L);

        List<Object[]> totalByAgent = Collections.singletonList(
                new Object[]{agent1, 85L}
        );
        when(incidentRepo.countIncidentsByAgent()).thenReturn(totalByAgent);

        List<Object[]> resolvedByAgent = Collections.singletonList(
                new Object[]{agent1, 60L}
        );
        when(incidentRepo.countResolvedIncidentsByAgent()).thenReturn(resolvedByAgent);
        when(incidentRepo.countIncidentsByDepartement()).thenReturn(Collections.emptyList());

        // When
        AdminReportData report = adminReportService.buildReport();

        // Then
        assertThat(report.agents()).hasSize(1);
        AgentStatsDTO stats = report.agents().get(0);
        assertThat(stats.totalIncidents()).isEqualTo(85L);
        assertThat(stats.incidentsTraites()).isEqualTo(60L);
    }

    @Test
    @DisplayName("Doit générer un rapport avec tous les départements")
    void devraitGenererRapportAvecTousDepartements() {
        // Given
        when(citoyenRepo.count()).thenReturn(200L);
        when(agentRepo.count()).thenReturn(30L);
        when(incidentRepo.count()).thenReturn(500L);
        when(incidentRepo.countByStatut(any())).thenReturn(100L);
        when(incidentRepo.countIncidentsByAgent()).thenReturn(Collections.emptyList());
        when(incidentRepo.countResolvedIncidentsByAgent()).thenReturn(Collections.emptyList());

        List<Object[]> allDepartements = Arrays.asList(
                new Object[]{Departement.VOIRIE, 150L},
                new Object[]{Departement.PROPRETE, 120L},
                new Object[]{Departement.SECURITE, 100L},
                new Object[]{Departement.ECLAIRAGE_PUBLIC, 80L},
                new Object[]{Departement.INFRASTRUCTURE, 50L}
        );
        when(incidentRepo.countIncidentsByDepartement()).thenReturn(allDepartements);

        // When
        AdminReportData report = adminReportService.buildReport();

        // Then
        assertThat(report.incidentsParDepartement()).hasSize(5);

        assertThat(report.incidentsParDepartement())
                .extracting(IncidentByDepartementDTO::departement)
                .containsExactly(
                        Departement.VOIRIE,
                        Departement.PROPRETE,
                        Departement.SECURITE,
                        Departement.ECLAIRAGE_PUBLIC,
                        Departement.INFRASTRUCTURE
                );

        assertThat(report.incidentsParDepartement())
                .extracting(IncidentByDepartementDTO::total)
                .containsExactly(150L, 120L, 100L, 80L, 50L);
    }

    @Test
    @DisplayName("Doit gérer des valeurs nulles dans les statistiques")
    void devraitGererValeursNulles() {
        // Given
        when(citoyenRepo.count()).thenReturn(0L);
        when(agentRepo.count()).thenReturn(0L);
        when(incidentRepo.count()).thenReturn(0L);
        when(incidentRepo.countByStatut(any())).thenReturn(0L);
        when(incidentRepo.countIncidentsByAgent()).thenReturn(Collections.emptyList());
        when(incidentRepo.countResolvedIncidentsByAgent()).thenReturn(Collections.emptyList());
        when(incidentRepo.countIncidentsByDepartement()).thenReturn(Collections.emptyList());

        // When
        AdminReportData report = adminReportService.buildReport();

        // Then
        assertThat(report).isNotNull();
        assertThat(report.totalCitoyens()).isZero();
        assertThat(report.totalAgents()).isZero();
        assertThat(report.totalIncidents()).isZero();
        assertThat(report.agents()).isEmpty();
        assertThat(report.incidentsParDepartement()).isEmpty();
    }
}