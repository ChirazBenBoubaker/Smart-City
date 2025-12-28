package com.example.smartcity;

import com.example.smartcity.config.TestDataLoader;
import com.example.smartcity.config.TestSecurityConfig;
import com.example.smartcity.dao.AgentMunicipalRepository;
import com.example.smartcity.dao.CitoyenRepository;
import com.example.smartcity.dao.IncidentRepository;
import com.example.smartcity.dto.AdminReportData;
import com.example.smartcity.metier.service.AdminReportPdfService;
import com.example.smartcity.metier.service.AdminReportService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test d'intégration simple pour vérifier que tout fonctionne
 * Exécutez : mvn test -Dtest=SimpleIntegrationTest
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestDataLoader.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Test d'intégration simple")
class SimpleIntegrationTest {

    @Autowired
    private CitoyenRepository citoyenRepo;

    @Autowired
    private AgentMunicipalRepository agentRepo;

    @Autowired
    private IncidentRepository incidentRepo;

    @Autowired
    private AdminReportService adminReportService;

    @Autowired
    private AdminReportPdfService pdfService;

    @Autowired
    private TestDataLoader.TestDataInitializer testDataInitializer;

    @BeforeEach
    void setUp() {
        System.out.println("\n=== Chargement des données de test ===");
        testDataInitializer.loadTestData();
    }

    @Test
    @Order(1)
    @DisplayName("1. Vérifier que les données sont chargées")
    void test01_VerifierDonneesChargees() {
        System.out.println("\n=== TEST 1: Vérification des données ===");

        long citoyens = citoyenRepo.count();
        long agents = agentRepo.count();
        long incidents = incidentRepo.count();

        System.out.println("Citoyens: " + citoyens);
        System.out.println("Agents: " + agents);
        System.out.println("Incidents: " + incidents);

        assertThat(citoyens).isEqualTo(3);
        assertThat(agents).isEqualTo(3);
        assertThat(incidents).isEqualTo(6);

        System.out.println("✅ Test 1 réussi !");
    }


}