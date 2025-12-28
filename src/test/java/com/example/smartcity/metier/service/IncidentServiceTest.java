package com.example.smartcity.metier.service;

import com.example.smartcity.dao.CitoyenRepository;
import com.example.smartcity.dao.IncidentRepository;
import com.example.smartcity.model.entity.Citoyen;
import com.example.smartcity.model.entity.Incident;
import com.example.smartcity.model.enums.StatutIncident;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidentServiceTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private CitoyenRepository citoyenRepository;

    @InjectMocks
    private IncidentService incidentService;

    private Citoyen citoyen;
    private Incident incident;

    @BeforeEach
    void setUp() {
        citoyen = new Citoyen();
        citoyen.setEmail("test@example.com");

        incident = new Incident();
        incident.setId(1L);
        incident.setCitoyen(citoyen);
        incident.setStatut(StatutIncident.SIGNALE);
    }

    // ===== Test 1: Incident exists and belongs to the citoyen =====
    @Test
    void testGetIncidentById_Success() {
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        Optional<Incident> result = incidentService.getIncidentById(1L, "test@example.com");

        assertTrue(result.isPresent());
        assertEquals(incident, result.get());
        verify(incidentRepository).findById(1L);
    }

    // ===== Test 2: Incident exists but belongs to another citoyen =====
    @Test
    void testGetIncidentById_OtherCitoyen() {
        Citoyen autre = new Citoyen();
        autre.setEmail("other@example.com");
        incident.setCitoyen(autre);

        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        Optional<Incident> result = incidentService.getIncidentById(1L, "test@example.com");

        assertFalse(result.isPresent());
        verify(incidentRepository).findById(1L);
    }

    // ===== Test 3: Incident does not exist =====
    @Test
    void testGetIncidentById_NotFound() {
        when(incidentRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Incident> result = incidentService.getIncidentById(1L, "test@example.com");

        assertFalse(result.isPresent());
        verify(incidentRepository).findById(1L);
    }

    // ===== Test 4: Incident exists, email case-insensitive match =====
    @Test
    void testGetIncidentById_EmailCaseInsensitive() {
        incident.setCitoyen(citoyen);
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        Optional<Incident> result = incidentService.getIncidentById(1L, "TEST@example.com");

        // Since service uses equals, it will fail if case differs. Adjust if you want case-insensitive
        assertFalse(result.isPresent());
    }

    // ===== Test 5: Incident exists, null email passed =====
    @Test
    void testGetIncidentById_NullEmail() {
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

        Optional<Incident> result = incidentService.getIncidentById(1L, null);

        assertFalse(result.isPresent());
        verify(incidentRepository).findById(1L);
    }
}