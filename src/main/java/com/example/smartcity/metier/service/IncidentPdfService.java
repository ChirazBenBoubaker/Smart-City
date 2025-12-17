package com.example.smartcity.metier.service;

import com.example.smartcity.model.entity.*;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.element.*;
import org.springframework.stereotype.Service;
import com.itextpdf.layout.Document;


import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.borders.Border;



@Service
public class IncidentPdfService {

    public void generateIncidentPdf(Incident incident, OutputStream os) {

        PdfWriter writer = new PdfWriter(os);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf);

        // ===== HEADER =====
        doc.add(new Paragraph("SMART CITY – Rapport d'incident")
                .setBold()
                .setFontSize(20)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.DARK_GRAY));

        doc.add(new Paragraph("Généré le : " + now())
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY)
                .setMarginBottom(20));

        // ===== SECTIONS =====
        section(doc, "Informations générales", table(
                row("Titre", incident.getTitre()),
                row("Catégorie", incident.getCategorie().name()),
                row("Priorité", incident.getPriorite().name()),
                row("Statut", incident.getStatut().name()),
                row("Description", incident.getDescription())
        ));

        section(doc, "Citoyen déclarant",
                incident.getCitoyen() != null
                        ? table(
                        row("Nom", fullName(incident.getCitoyen())),
                        row("Email", incident.getCitoyen().getEmail()),
                        row("Téléphone", incident.getCitoyen().getTelephone()),
                        row("Date d'inscription",
                                formatDate(incident.getCitoyen().getDateInscription()))
                )
                        : empty("Aucun citoyen associé")
        );

        section(doc, "Agent responsable",
                incident.getAgentResponsable() != null
                        ? table(
                        row("Nom", fullName(incident.getAgentResponsable())),
                        row("Email", incident.getAgentResponsable().getEmail()),
                        row("Téléphone", incident.getAgentResponsable().getTelephone()),
                        row("Département",
                                incident.getAgentResponsable().getDepartement().name())
                )
                        : empty("Aucun agent affecté")
        );

        section(doc, "Localisation",
                incident.getQuartier() != null
                        ? table(
                        row("Quartier", safe(incident.getQuartier().getNom())),
                        row("Rue", safe(incident.getQuartier().getRue())),
                        row("Ville", formatVille(incident.getQuartier())),
                        row("Gouvernorat", safe(incident.getQuartier().getGouvernorat())),
                        row("Latitude", value(incident.getLatitude())),
                        row("Longitude", value(incident.getLongitude()))
                )
                        : empty("Localisation non renseignée")
        );

        section(doc, "Historique", table(
                row("Date de signalement", formatDate(incident.getDateSignalement())),
                row("Date de prise en charge", formatDate(incident.getDatePriseEnCharge())),
                row("Date de résolution", formatDate(incident.getDateResolution())),
                row("Date de clôture", formatDate(incident.getDateCloture()))
        ));

        if (incident.getPhotos() != null && !incident.getPhotos().isEmpty()) {
            sectionTitle(doc, "Photos");
            for (Photo p : incident.getPhotos()) {
                try {
                    Image img = new Image(ImageDataFactory.create(p.getChemin()))
                            .scaleToFit(350, 250)
                            .setMarginBottom(10);
                    doc.add(img);
                } catch (Exception e) {
                    doc.add(new Paragraph("Image non chargée : " + p.getNomFichier())
                            .setItalic().setFontSize(9));
                }
            }
        }

        doc.close();
    }

    // ===== UI HELPERS =====

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
        Table t = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .useAllAvailableWidth()
                .setBorder(Border.NO_BORDER);

        for (Cell c : rows) t.addCell(c);
        return t;
    }

    private Cell row(String label, String value) {
        return new Cell(1, 2)
                .add(new Paragraph(label + " : " + safe(value)))
                .setBorder(Border.NO_BORDER);
    }

    private IBlockElement empty(String msg) {
        return new Paragraph(msg).setItalic().setFontSize(11);
    }

    // ===== DATA HELPERS =====

    private String fullName(User u) {
        return safe(u.getNom()) + " " + safe(u.getPrenom());
    }

    private String formatVille(Quartier q) {
        String v = safe(q.getVille());
        String cp = safe(q.getCodePostal());
        return cp.equals("-") ? v : v + " (" + cp + ")";
    }

    private String safe(String v) {
        if (v == null) return "-";
        v = v.trim();
        return v.isEmpty() || v.equalsIgnoreCase("Inconnu") ? "-" : v;
    }

    private String value(Double d) {
        return d != null ? d.toString() : "-";
    }

    private String formatDate(LocalDateTime d) {
        return d != null
                ? d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "-";
    }

    private String now() {
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}
