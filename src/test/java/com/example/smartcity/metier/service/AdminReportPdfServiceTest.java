package com.example.smartcity.metier.service;

import com.example.smartcity.dto.AdminReportData;
import com.example.smartcity.dto.AgentStatsDTO;
import com.example.smartcity.dto.IncidentByDepartementDTO;
import com.example.smartcity.model.enums.Departement;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// ✅ Test unitaire simple sans contexte Spring
@DisplayName("Tests du service AdminReportPdfService")
class AdminReportPdfServiceTest {

    private AdminReportPdfService pdfService;

    @BeforeEach
    void setUp() {
        pdfService = new AdminReportPdfService();
    }

    @Test
    @DisplayName("Doit générer un PDF valide avec toutes les données")
    void devraitGenererPdfValideAvecToutesDonnees() throws Exception {
        // Given
        List<AgentStatsDTO> agents = Arrays.asList(
                new AgentStatsDTO("Dupont", "Jean", Departement.VOIRIE, 50L, 30L),
                new AgentStatsDTO("Martin", "Marie", Departement.PROPRETE, 35L, 25L)
        );

        List<IncidentByDepartementDTO> departements = Arrays.asList(
                new IncidentByDepartementDTO(Departement.VOIRIE, 150L),
                new IncidentByDepartementDTO(Departement.PROPRETE, 100L)
        );

        AdminReportData reportData = new AdminReportData(
                200L,  // totalCitoyens
                30L,   // totalAgents
                500L,  // totalIncidents
                80L,   // incidentsSignales
                150L,  // incidentsPrisEnCharge
                180L,  // incidentsResolus
                90L,   // incidentsClotures
                agents,
                departements
        );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // When
        pdfService.generate(reportData, outputStream);

        // Then
        byte[] pdfBytes = outputStream.toByteArray();
        assertThat(pdfBytes).isNotEmpty();
        assertThat(pdfBytes.length).isGreaterThan(1000);

        // Vérifier que c'est un PDF valide
        PdfDocument pdfDoc = new PdfDocument(new PdfReader(new ByteArrayInputStream(pdfBytes)));
        assertThat(pdfDoc.getNumberOfPages()).isGreaterThan(0);
        pdfDoc.close();
    }

    @Test
    @DisplayName("Doit générer un PDF même sans agents")
    void devraitGenererPdfSansAgents() throws Exception {
        // Given
        AdminReportData reportData = new AdminReportData(
                100L, 10L, 50L, 20L, 15L, 10L, 5L,
                Collections.emptyList(),
                Arrays.asList(new IncidentByDepartementDTO(Departement.VOIRIE, 50L))
        );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // When
        pdfService.generate(reportData, outputStream);

        // Then
        byte[] pdfBytes = outputStream.toByteArray();
        assertThat(pdfBytes).isNotEmpty();

        PdfDocument pdfDoc = new PdfDocument(new PdfReader(new ByteArrayInputStream(pdfBytes)));
        assertThat(pdfDoc.getNumberOfPages()).isGreaterThan(0);
        pdfDoc.close();
    }

    @Test
    @DisplayName("Doit générer un PDF même sans départements")
    void devraitGenererPdfSansDepartements() throws Exception {
        // Given
        List<AgentStatsDTO> agents = Collections.singletonList(
                new AgentStatsDTO("Test", "Agent", Departement.VOIRIE, 10L, 5L)
        );

        AdminReportData reportData = new AdminReportData(
                50L, 5L, 25L, 10L, 8L, 5L, 2L,
                agents,
                Collections.emptyList()
        );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // When
        pdfService.generate(reportData, outputStream);

        // Then
        byte[] pdfBytes = outputStream.toByteArray();
        assertThat(pdfBytes).isNotEmpty();

        PdfDocument pdfDoc = new PdfDocument(new PdfReader(new ByteArrayInputStream(pdfBytes)));
        assertThat(pdfDoc.getNumberOfPages()).isGreaterThan(0);
        pdfDoc.close();
    }

    @Test
    @DisplayName("Doit générer un PDF avec données nulles pour agents et départements")
    void devraitGenererPdfAvecDonneesNulles() throws Exception {
        // Given
        AdminReportData reportData = new AdminReportData(
                0L, 0L, 0L, 0L, 0L, 0L, 0L,
                null,
                null
        );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // When
        pdfService.generate(reportData, outputStream);

        // Then
        byte[] pdfBytes = outputStream.toByteArray();
        assertThat(pdfBytes).isNotEmpty();

        PdfDocument pdfDoc = new PdfDocument(new PdfReader(new ByteArrayInputStream(pdfBytes)));
        assertThat(pdfDoc.getNumberOfPages()).isGreaterThan(0);
        pdfDoc.close();
    }

    @Test
    @DisplayName("Doit générer un PDF avec de nombreux agents")
    void devraitGenererPdfAvecBeaucoupDAgents() throws Exception {
        // Given
        List<AgentStatsDTO> agents = Arrays.asList(
                new AgentStatsDTO("Agent1", "Test1", Departement.VOIRIE, 100L, 80L),
                new AgentStatsDTO("Agent2", "Test2", Departement.PROPRETE, 90L, 70L),
                new AgentStatsDTO("Agent3", "Test3", Departement.ECLAIRAGE_PUBLIC, 85L, 65L),
                new AgentStatsDTO("Agent4", "Test4", Departement.INFRASTRUCTURE, 75L, 55L),
                new AgentStatsDTO("Agent5", "Test5", Departement.SECURITE, 70L, 50L)
        );

        AdminReportData reportData = new AdminReportData(
                500L, 50L, 1000L, 200L, 400L, 300L, 100L,
                agents,
                Collections.emptyList()
        );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // When
        pdfService.generate(reportData, outputStream);

        // Then
        byte[] pdfBytes = outputStream.toByteArray();
        assertThat(pdfBytes).isNotEmpty();
        assertThat(pdfBytes.length).isGreaterThan(1500);
    }

    @Test
    @DisplayName("Doit générer un PDF avec tous les départements")
    void devraitGenererPdfAvecTousDepartements() throws Exception {
        // Given
        List<IncidentByDepartementDTO> departements = Arrays.asList(
                new IncidentByDepartementDTO(Departement.VOIRIE, 200L),
                new IncidentByDepartementDTO(Departement.PROPRETE, 150L),
                new IncidentByDepartementDTO(Departement.ECLAIRAGE_PUBLIC, 120L),
                new IncidentByDepartementDTO(Departement.INFRASTRUCTURE, 100L),
                new IncidentByDepartementDTO(Departement.SECURITE, 80L)
        );

        AdminReportData reportData = new AdminReportData(
                300L, 40L, 650L, 150L, 250L, 200L, 50L,
                Collections.emptyList(),
                departements
        );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // When
        pdfService.generate(reportData, outputStream);

        // Then
        byte[] pdfBytes = outputStream.toByteArray();
        assertThat(pdfBytes).isNotEmpty();

        PdfDocument pdfDoc = new PdfDocument(new PdfReader(new ByteArrayInputStream(pdfBytes)));
        assertThat(pdfDoc.getNumberOfPages()).isGreaterThan(0);
        pdfDoc.close();
    }

    @Test
    @DisplayName("Doit générer un PDF minimal avec des zéros")
    void devraitGenererPdfMinimalAvecZeros() throws Exception {
        // Given
        AdminReportData reportData = new AdminReportData(
                0L, 0L, 0L, 0L, 0L, 0L, 0L,
                Collections.emptyList(),
                Collections.emptyList()
        );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // When
        pdfService.generate(reportData, outputStream);

        // Then
        byte[] pdfBytes = outputStream.toByteArray();
        assertThat(pdfBytes).isNotEmpty();
    }

    @Test
    @DisplayName("Doit lever une exception si le flux de sortie est null")
    void devraitLeverExceptionSiOutputStreamNull() {
        // Given
        AdminReportData reportData = new AdminReportData(
                100L, 10L, 50L, 20L, 15L, 10L, 5L,
                Collections.emptyList(),
                Collections.emptyList()
        );

        // When & Then
        assertThatThrownBy(() -> pdfService.generate(reportData, null))
                .isInstanceOf(NullPointerException.class);
    }

}