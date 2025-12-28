package com.example.smartcity.config;

import com.example.smartcity.dao.AgentMunicipalRepository;
import com.example.smartcity.dao.CitoyenRepository;
import com.example.smartcity.dao.IncidentRepository;
import com.example.smartcity.model.entity.AgentMunicipal;
import com.example.smartcity.model.entity.Citoyen;
import com.example.smartcity.model.entity.Incident;
import com.example.smartcity.model.enums.Departement;
import com.example.smartcity.model.enums.PrioriteIncident;
import com.example.smartcity.model.enums.RoleUtilisateur;
import com.example.smartcity.model.enums.StatutIncident;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

/**
 * Configuration de test pour charger des données dans H2
 */
@TestConfiguration
@Profile("test")
public class TestDataLoader {

    @Bean
    public TestDataInitializer testDataInitializer(
            CitoyenRepository citoyenRepo,
            AgentMunicipalRepository agentRepo,
            IncidentRepository incidentRepo,
            PasswordEncoder passwordEncoder
    ) {
        return new TestDataInitializer(citoyenRepo, agentRepo, incidentRepo, passwordEncoder);
    }

    public static class TestDataInitializer {

        private final CitoyenRepository citoyenRepo;
        private final AgentMunicipalRepository agentRepo;
        private final IncidentRepository incidentRepo;
        private final PasswordEncoder passwordEncoder;

        public TestDataInitializer(
                CitoyenRepository citoyenRepo,
                AgentMunicipalRepository agentRepo,
                IncidentRepository incidentRepo,
                PasswordEncoder passwordEncoder
        ) {
            this.citoyenRepo = citoyenRepo;
            this.agentRepo = agentRepo;
            this.incidentRepo = incidentRepo;
            this.passwordEncoder = passwordEncoder;
        }

        public void loadTestData() {
            // Nettoyer
            incidentRepo.deleteAll();
            agentRepo.deleteAll();
            citoyenRepo.deleteAll();

            // Créer des citoyens
            Citoyen citoyen1 = createCitoyen("Martin", "Sophie", "sophie.martin@test.com");
            Citoyen citoyen2 = createCitoyen("Dupont", "Pierre", "pierre.dupont@test.com");
            Citoyen citoyen3 = createCitoyen("Bernard", "Marie", "marie.bernard@test.com");

            citoyen1 = citoyenRepo.save(citoyen1);
            citoyen2 = citoyenRepo.save(citoyen2);
            citoyen3 = citoyenRepo.save(citoyen3);

            // Créer des agents
            AgentMunicipal agent1 = createAgent("Dubois", "Jean", "jean.dubois@ville.fr", Departement.VOIRIE);
            AgentMunicipal agent2 = createAgent("Lefebvre", "Alice", "alice.lefebvre@ville.fr", Departement.PROPRETE);
            AgentMunicipal agent3 = createAgent("Moreau", "Thomas", "thomas.moreau@ville.fr", Departement.SECURITE);

            agent1 = agentRepo.save(agent1);
            agent2 = agentRepo.save(agent2);
            agent3 = agentRepo.save(agent3);

            // Créer des incidents
            createAndSaveIncident(citoyen1, agent1, "Nid de poule", "Grand trou dangereux",
                    Departement.VOIRIE, StatutIncident.PRIS_EN_CHARGE);

            createAndSaveIncident(citoyen1, agent1, "Fissure chaussée", "Fissure importante",
                    Departement.VOIRIE, StatutIncident.RESOLU);

            createAndSaveIncident(citoyen2, agent2, "Déchets sauvages", "Tas de déchets",
                    Departement.PROPRETE, StatutIncident.SIGNALE);

            createAndSaveIncident(citoyen2, agent2, "Poubelle cassée", "Poubelle endommagée",
                    Departement.PROPRETE, StatutIncident.RESOLU);

            createAndSaveIncident(citoyen3, agent3, "Lampadaire éteint", "Pas d'éclairage",
                    Departement.ECLAIRAGE_PUBLIC, StatutIncident.SIGNALE);

            createAndSaveIncident(citoyen3, null, "Trottoir abîmé", "Trottoir dangereux",
                    Departement.INFRASTRUCTURE, StatutIncident.SIGNALE);

            System.out.println("✅ Données de test chargées:");
            System.out.println("   - " + citoyenRepo.count() + " citoyens");
            System.out.println("   - " + agentRepo.count() + " agents");
            System.out.println("   - " + incidentRepo.count() + " incidents");
        }

        private Citoyen createCitoyen(String nom, String prenom, String email) {
            Citoyen citoyen = new Citoyen();
            citoyen.setNom(nom);
            citoyen.setPrenom(prenom);
            citoyen.setEmail(email);
            citoyen.setTelephone("0612345678");
            citoyen.setRole(RoleUtilisateur.CITOYEN);
            citoyen.setPassword(passwordEncoder.encode("password"));
            citoyen.setEnabled(true);
            return citoyen;
        }

        private AgentMunicipal createAgent(String nom, String prenom, String email, Departement departement) {
            AgentMunicipal agent = new AgentMunicipal();
            agent.setNom(nom);
            agent.setPrenom(prenom);
            agent.setEmail(email);
            agent.setTelephone("0612345679");
            agent.setDepartement(departement);
            agent.setRole(RoleUtilisateur.AGENT_MUNICIPAL);
            agent.setPassword(passwordEncoder.encode("password"));
            agent.setEnabled(true);
            agent.setEnService(true);
            return agent;
        }

        private void createAndSaveIncident(Citoyen citoyen, AgentMunicipal agent,
                                           String titre, String description,
                                           Departement categorie, StatutIncident statut) {
            Incident incident = new Incident();
            incident.setTitre(titre);
            incident.setDescription(description);
            incident.setCategorie(categorie);
            incident.setStatut(statut);
            incident.setPriorite(PrioriteIncident.MOYENNE);
            incident.setLatitude(48.8566);
            incident.setLongitude(2.3522);
            incident.setDateSignalement(LocalDateTime.now().minusDays(5));
            incident.setCitoyen(citoyen);

            if (agent != null) {
                incident.setAgentResponsable(agent);
                incident.setDatePriseEnCharge(LocalDateTime.now().minusDays(3));
            }

            if (statut == StatutIncident.RESOLU) {
                incident.setDateResolution(LocalDateTime.now().minusDays(1));
            } else if (statut == StatutIncident.CLOTURE) {
                incident.setDateResolution(LocalDateTime.now().minusDays(2));
                incident.setDateCloture(LocalDateTime.now().minusDays(1));
            }

            incidentRepo.save(incident);
        }
    }
}