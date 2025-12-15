package com.example.smartcity.metier.service;

import com.example.smartcity.dao.QuartierRepository;
import com.example.smartcity.model.entity.Quartier;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class QuartierService {

    private final QuartierRepository quartierRepository;
    private final RestTemplate restTemplate;

    public QuartierService(QuartierRepository quartierRepository) {
        this.quartierRepository = quartierRepository;
        this.restTemplate = new RestTemplate();
    }

    public Quartier getQuartierFromCoordinates(Double lat, Double lon) {

        // 🌍 API OpenStreetMap Nominatim (reverse geocoding)
        String url = "https://nominatim.openstreetmap.org/reverse"
                + "?format=json"
                + "&lat=" + lat
                + "&lon=" + lon
                + "&zoom=18"
                + "&addressdetails=1";

        // 🧾 Headers obligatoires (User-Agent requis par Nominatim)
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "SmartCityApp/1.0");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response =
                restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

        if (response.getBody() == null) {
            throw new RuntimeException("Impossible de récupérer l'adresse depuis OpenStreetMap");
        }

        Map body = response.getBody();
        Map address = (Map) body.get("address");

        // 🏘️ Récupération des infos quartier
        String quartierNom = (String) address.getOrDefault(
                "suburb",
                address.getOrDefault("neighbourhood", "Inconnu")
        );

        String ville = (String) address.getOrDefault("city",
                address.getOrDefault("town",
                        address.getOrDefault("village", "Inconnue")));

        String codePostal = (String) address.getOrDefault("postcode", "00000");

        String gouvernorat = (String) address.getOrDefault(
                "state",
                address.getOrDefault("region", "Inconnu")
        );
        String rue = (String) address.getOrDefault(
                "road",
                address.getOrDefault(
                        "pedestrian",
                        address.getOrDefault("residential", "Inconnue")
                )
        );

        // 🔎 Vérifier si le quartier existe déjà
        return quartierRepository
                .findByNomAndVille(quartierNom, ville)
                .orElseGet(() -> {
                    Quartier q = new Quartier();
                    q.setNom(quartierNom);
                    q.setVille(ville);
                    q.setCodePostal(codePostal);
                    q.setGouvernorat(gouvernorat);
                    q.setRue(rue);
                    return quartierRepository.save(q);
                });
    }
}
