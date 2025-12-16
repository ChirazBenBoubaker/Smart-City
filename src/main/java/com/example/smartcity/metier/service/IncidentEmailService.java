package com.example.smartcity.metier.service;

import com.example.smartcity.model.entity.AgentMunicipal;
import com.example.smartcity.model.entity.Incident;
import org.springframework.stereotype.Service;


    @Service
    public class IncidentEmailService {

        public String buildAssignIncidentEmail(AgentMunicipal agent, Incident incident) {

            return """
            <div style="font-family:Inter,Arial,sans-serif;line-height:1.6;color:#1f2937">
                <h2 style="color:#16a34a;">Nouvel incident affecté</h2>

                <p>Bonjour <strong>%s %s</strong>,</p>

                <p>
                    Un nouvel incident vous a été affecté sur la plateforme
                    <strong>Smart City</strong>.
                </p>

                <hr/>

                <p><strong>📌 Incident :</strong> #%d</p>
                <p><strong>📝 Titre :</strong> %s</p>
                <p><strong>📂 Catégorie :</strong> %s</p>
                <p><strong>⚠️ Priorité :</strong> %s</p>
                <p><strong>📅 Date :</strong> %s</p>

                <hr/>

                <p>
                    👉 Pour plus de détails, veuillez consulter votre tableau de bord :
                </p>

                <a href="http://localhost:8080/agent/dashboard"
                   style="display:inline-block;margin-top:12px;
                          background:#16a34a;color:white;
                          padding:10px 18px;
                          border-radius:8px;
                          text-decoration:none;
                          font-weight:600;">
                    Accéder au dashboard
                </a>

                <p style="margin-top:24px;color:#6b7280;font-size:13px">
                    Ceci est un message automatique. Merci de ne pas répondre.
                </p>
            </div>
            """
                    .formatted(
                            agent.getPrenom(),
                            agent.getNom(),
                            incident.getId(),
                            incident.getTitre(),
                            incident.getCategorie(),
                            incident.getPriorite(),
                            incident.getDateSignalement()
                    );
        }
    }


