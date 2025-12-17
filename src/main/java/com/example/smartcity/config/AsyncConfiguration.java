package com.example.smartcity.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Configuration pour activer l'exécution asynchrone des méthodes
 * Utilisé notamment pour l'envoi d'emails en arrière-plan
 */
@Configuration
@EnableAsync
public class AsyncConfiguration {
    // Spring Boot configure automatiquement l'executor avec les propriétés
    // définies dans application.properties (spring.task.execution.*)
}