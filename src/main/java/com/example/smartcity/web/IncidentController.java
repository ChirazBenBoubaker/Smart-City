package com.example.smartcity.web;

import com.example.smartcity.dao.CitoyenRepository;
import com.example.smartcity.dao.IncidentRepository;
import com.example.smartcity.model.entity.Citoyen;
import com.example.smartcity.model.entity.Incident;
import com.example.smartcity.model.entity.Quartier;
import com.example.smartcity.model.enums.Departement;
import com.example.smartcity.model.enums.PrioriteIncident;
import com.example.smartcity.model.enums.StatutIncident;
import com.example.smartcity.metier.service.QuartierService;
import com.example.smartcity.metier.service.PhotoService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/incidents")
@CrossOrigin("*")
public class IncidentController {

    private final IncidentRepository incidentRepository;
    private final CitoyenRepository citoyenRepository;
    private final QuartierService quartierService;
    private final PhotoService photoService;

    public IncidentController(
            IncidentRepository incidentRepository,
            CitoyenRepository citoyenRepository,
            QuartierService quartierService,
            PhotoService photoService
    ) {
        this.incidentRepository = incidentRepository;
        this.citoyenRepository = citoyenRepository;
        this.quartierService = quartierService;
        this.photoService = photoService;
    }




    @PreAuthorize("hasRole('CITOYEN')")
    @PostMapping(consumes = "multipart/form-data")
    public Incident declarerIncident(
            @RequestParam String titre,
            @RequestParam String description,
            @RequestParam Departement categorie,
            @RequestParam PrioriteIncident priorite,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false) MultipartFile[] photos
    ) throws Exception {

        // 🔐 Récupération du citoyen authentifié
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName(); // Email du citoyen connecté

        Citoyen citoyen = citoyenRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Citoyen non trouvé"));

        // 📍 Détection automatique du quartier
        Quartier quartier = quartierService.getQuartierFromCoordinates(latitude, longitude);

        // 🧱 Création de l'incident
        Incident incident = new Incident();
        incident.setTitre(titre);
        incident.setDescription(description);
        incident.setCategorie(categorie);
        incident.setPriorite(priorite);
        incident.setLatitude(latitude);
        incident.setLongitude(longitude);
        incident.setQuartier(quartier);
        incident.setCitoyen(citoyen); // ✅ Association automatique du citoyen
        incident.setStatut(StatutIncident.SIGNALE);
        incident.setDateSignalement(LocalDateTime.now());

        // 💾 Sauvegarde incident
        Incident savedIncident = incidentRepository.save(incident);

        // 📸 Sauvegarde des photos (si présentes)
        if (photos != null && photos.length > 0) {
            photoService.savePhotos(photos, savedIncident);
        }

        // 🖨️ LOG CONSOLE
        System.out.println("===== INCIDENT ENREGISTRÉ =====");
        System.out.println("ID : " + savedIncident.getId());
        System.out.println("Citoyen : " + citoyen.getPrenom() + " " + citoyen.getNom());
        System.out.println("Email : " + citoyen.getEmail());
        System.out.println("Titre : " + savedIncident.getTitre());
        System.out.println("Quartier : " + quartier.getNom());
        System.out.println("Ville : " + quartier.getVille());
        System.out.println("Gouvernorat : " + quartier.getGouvernorat());
        System.out.println("Rue : " + quartier.getRue());
        System.out.println("Latitude : " + savedIncident.getLatitude());
        System.out.println("Longitude : " + savedIncident.getLongitude());
        System.out.println("Photos : " + (photos != null ? photos.length : 0));
        System.out.println("===============================");

        return savedIncident;
    }
}