package com.example.smartcity.metier.imp;

import com.example.smartcity.dao.NotificationRepository;
import com.example.smartcity.dao.UserRepository;
import com.example.smartcity.dao.VerificationTokenRepository;
import com.example.smartcity.dto.RegisterRequest;
import com.example.smartcity.metier.service.AuthService;
import com.example.smartcity.metier.service.EmailService;
import com.example.smartcity.model.entity.Citoyen;
import com.example.smartcity.model.entity.Notification;
import com.example.smartcity.model.entity.User;
import com.example.smartcity.model.entity.VerificationToken;
import com.example.smartcity.model.enums.RoleUtilisateur;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public void registerCitoyen(RegisterRequest request, String appUrl) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }

        // 1) Créer citoyen (désactivé tant que non vérifié)
        Citoyen c = new Citoyen();
        c.setNom(request.getNom());
        c.setPrenom(request.getPrenom());
        c.setEmail(request.getEmail());
        c.setTelephone(request.getTelephone());
        c.setPassword(passwordEncoder.encode(request.getPassword()));
        c.setRole(RoleUtilisateur.CITOYEN);
        c.setEnabled(false);
        userRepository.save(c);

        // 2) Générer token
        String token = UUID.randomUUID().toString();
        VerificationToken vt = new VerificationToken();
        vt.setToken(token);
        vt.setUser(c);
        vt.setExpiresAt(LocalDateTime.now().plusHours(24));
        tokenRepository.save(vt);

        // 3) Construire lien
        String link = appUrl + "/verify?token=" + token;

        // 4) Email HTML
        String html = """
            <div style="font-family:Inter,Arial,sans-serif">
              <h2>Vérification de votre compte Smart City</h2>
              <p>Bonjour %s %s,</p>
              <p>Merci de confirmer votre email en cliquant sur ce lien :</p>
              <p><a href="%s">✅ Vérifier mon email</a></p>
              <p>Ce lien expire dans 24 heures.</p>
            </div>
        """.formatted(c.getPrenom(), c.getNom(), link);

        emailService.send(c.getEmail(), "Vérifiez votre email - Smart City", html);

        // 5) (Optionnel) Sauver une notification en DB
        Notification n = new Notification();
        n.setTitre("Vérification Email");
        n.setMessage("Email de vérification envoyé à " + c.getEmail());
        n.setUtilisateur(c);
        notificationRepository.save(n);
    }

    @Override
    public void verifyEmail(String token) {
        VerificationToken vt = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token invalide"));

        if (vt.getConfirmedAt() != null) {
            throw new RuntimeException("Email déjà vérifié");
        }
        if (vt.isExpired()) {
            throw new RuntimeException("Token expiré");
        }

        vt.setConfirmedAt(LocalDateTime.now());
        tokenRepository.save(vt);

        User u = vt.getUser();
        u.setEnabled(true);
        userRepository.save(u);

        // Notification DB (optionnel)
        Notification n = new Notification();
        n.setTitre("Compte activé");
        n.setMessage("Votre compte a été activé avec succès.");
        n.setUtilisateur(u);
        notificationRepository.save(n);
    }


}