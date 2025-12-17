package com.example.smartcity.metier.service;

import com.example.smartcity.dao.AgentMunicipalRepository;
import com.example.smartcity.model.entity.AgentMunicipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AgentService {

    private final AgentMunicipalRepository agentRepository;
    private final PasswordEncoder passwordEncoder;

    public AgentMunicipal getAgentByEmail(String email) {
        return agentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Agent non trouvé"));
    }
    @Transactional
    public boolean changePassword(
            String email,
            String currentPassword,
            String newPassword,
            String confirmPassword) {

        AgentMunicipal agent = agentRepository.findByEmail(email).orElse(null);
        if (agent == null) return false;

        // Vérifier ancien mot de passe
        if (!passwordEncoder.matches(currentPassword, agent.getPassword())) {
            return false;
        }

        // Vérifier confirmation
        if (!newPassword.equals(confirmPassword)) {
            return false;
        }

        // Encoder nouveau mot de passe
        agent.setPassword(passwordEncoder.encode(newPassword));
        agentRepository.save(agent);

        return true;
    }
    @Transactional
    public void updateProfile(String email, String prenom, String nom, String telephone) {

        AgentMunicipal agent = agentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Agent introuvable"));

        agent.setPrenom(prenom);
        agent.setNom(nom);
        agent.setTelephone(telephone);

        agentRepository.save(agent);
    }


}