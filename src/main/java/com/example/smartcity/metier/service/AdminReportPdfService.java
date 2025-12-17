package com.example.smartcity.metier.service;

import com.example.smartcity.dto.AdminReportData;
import com.example.smartcity.dto.AgentStatsDTO;
import com.example.smartcity.dto.IncidentByDepartementDTO;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class AdminReportPdfService {

    public void generate(AdminReportData data, OutputStream os) {

        PdfWriter writer = new PdfWriter(os);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf);

        // ===== HEADER =====
        doc.add(new Paragraph("SMART CITY – Rapport Administrateur (Logs)")
                .setBold()
                .setFontSize(20)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.DARK_GRAY));

        doc.add(new Paragraph("Généré le : " + now())
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY)
                .setMarginBottom(20));

        // ===== STATISTIQUES GLOBALES =====
        section(doc, "Statistiques globales", table(
                row("Total citoyens", String.valueOf(data.totalCitoyens())),
                row("Total agents", String.valueOf(data.totalAgents())),
                row("Total incidents", String.valueOf(data.totalIncidents())),
                row("Incidents signalés", String.valueOf(data.incidentsSignales())),
                row("Incidents pris en charge", String.valueOf(data.incidentsPrisEnCharge())),
                row("Incidents résolus", String.valueOf(data.incidentsResolus())),
                row("Incidents clôturés", String.valueOf(data.incidentsClotures()))
        ));

        // ===== STATISTIQUES PAR AGENT =====
        sectionTitle(doc, "Statistiques par agent");

        if (data.agents() == null || data.agents().isEmpty()) {
            doc.add(new Paragraph("Aucune donnée agent disponible")
                    .setItalic().setFontSize(11));
        } else {

            Table agentTable = new Table(UnitValue.createPercentArray(
                    new float[]{25, 20, 20, 15, 20}
            )).useAllAvailableWidth();

            header(agentTable, "Nom");
            header(agentTable, "Prénom");
            header(agentTable, "Département");
            header(agentTable, "Affectés");
            header(agentTable, "Traités");

            for (AgentStatsDTO a : data.agents()) {
                cell(agentTable, a.nom());
                cell(agentTable, a.prenom());
                cell(agentTable, a.departement().name());
                cell(agentTable, String.valueOf(a.totalIncidents()));
                cell(agentTable, String.valueOf(a.incidentsTraites()));
            }

            doc.add(agentTable);
        }
// ===== INCIDENTS PAR DÉPARTEMENT =====
        sectionTitle(doc, "Incidents par département");

        if (data.incidentsParDepartement() == null
                || data.incidentsParDepartement().isEmpty()) {

            doc.add(new Paragraph("Aucune donnée disponible")
                    .setItalic()
                    .setFontSize(11));

        } else {

            Table depTable = new Table(
                    UnitValue.createPercentArray(new float[]{50, 50})
            ).useAllAvailableWidth();

            header(depTable, "Département");
            header(depTable, "Nombre d'incidents");

            for (IncidentByDepartementDTO d : data.incidentsParDepartement()) {
                cell(depTable, d.departement().name());
                cell(depTable, String.valueOf(d.total()));
            }

            doc.add(depTable);
        }

        doc.close(); // 🔥 OBLIGATOIRE
    }

    // ===== UI HELPERS (IDENTIQUES À TON STYLE) =====

    private void section(Document doc, String title, IBlockElement content) {
        sectionTitle(doc, title);
        doc.add(content);
    }

    private void sectionTitle(Document doc, String title) {
        doc.add(new Paragraph(title)
                .setBold()
                .setFontSize(14)
                .setMarginTop(15)
                .setMarginBottom(5));
    }

    private Table table(Cell... rows) {
        Table t = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                .useAllAvailableWidth()
                .setBorder(Border.NO_BORDER);

        for (Cell c : rows) t.addCell(c);
        return t;
    }

    private Cell row(String label, String value) {
        return new Cell(1, 2)
                .add(new Paragraph(label + " : " + value))
                .setBorder(Border.NO_BORDER);
    }

    private void header(Table t, String text) {
        t.addHeaderCell(new Cell()
                .add(new Paragraph(text).setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY));
    }

    private void cell(Table t, String text) {
        t.addCell(new Cell().add(new Paragraph(text)));
    }

    private String now() {
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}
