package com.example.smartcity.metier.service;

import com.example.smartcity.dao.AgentMunicipalRepository;
import com.example.smartcity.model.entity.AgentMunicipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AgentService {

    private final AgentMunicipalRepository agentRepository;

    public AgentMunicipal getAgentByEmail(String email) {
        return agentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Agent non trouvé"));
    }
}