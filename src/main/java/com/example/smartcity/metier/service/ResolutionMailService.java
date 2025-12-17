package com.example.smartcity.metier.service;

import com.example.smartcity.model.entity.Incident;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResolutionMailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    private static final String FROM_EMAIL = "noreply@smartcity.tn";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");

    /**
     * Envoie un email à l'administrateur pour l'informer de la résolution d'un incident
     */
    @Async
    public void envoyerEmailResolutionAdmin(Incident incident) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(FROM_EMAIL);
            helper.setTo("admin@smartcity.tn"); // Email admin par défaut
            helper.setSubject("✅ Incident #" + incident.getId() + " résolu - " + incident.getTitre());

            Context context = new Context();
            context.setVariable("incident", incident);
            context.setVariable("agentNom", incident.getAgentResponsable().getPrenom() + " " +
                    incident.getAgentResponsable().getNom());
            context.setVariable("dateResolution", incident.getDateResolution().format(DATE_FORMATTER));
            context.setVariable("dateSignalement", incident.getDateSignalement().format(DATE_FORMATTER));

            String htmlContent = templateEngine.process("emails/incident-resolu-admin", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email de résolution envoyé à l'admin pour l'incident #{}", incident.getId());

        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de l'email à l'admin pour l'incident #{}", incident.getId(), e);
        }
    }

    /**
     * Envoie un email au citoyen pour l'informer de la résolution et demander un feedback
     */
    @Async
    public void envoyerEmailResolutionCitoyen(Incident incident) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(FROM_EMAIL);
            helper.setTo(incident.getCitoyen().getEmail());
            helper.setSubject("✅ Votre incident #" + incident.getId() + " a été résolu !");

            Context context = new Context();
            context.setVariable("incident", incident);
            context.setVariable("citoyenPrenom", incident.getCitoyen().getPrenom());
            context.setVariable("dateResolution", incident.getDateResolution().format(DATE_FORMATTER));
            context.setVariable("dateSignalement", incident.getDateSignalement().format(DATE_FORMATTER));
            context.setVariable("feedbackUrl", "http://localhost:8082/citoyen/feedback/" + incident.getId());

            String htmlContent = templateEngine.process("emails/incident-resolu-citoyen", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email de résolution envoyé au citoyen {} pour l'incident #{}",
                    incident.getCitoyen().getEmail(), incident.getId());

        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de l'email au citoyen pour l'incident #{}",
                    incident.getId(), e);
        }
    }

    /**
     * Envoie les deux emails (admin + citoyen) lors de la résolution d'un incident
     */
    @Async
    public void notifierResolutionIncident(Incident incident) {
        envoyerEmailResolutionAdmin(incident);
        envoyerEmailResolutionCitoyen(incident);
    }
}