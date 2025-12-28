package com.example.smartcity.metier.service;

import com.example.smartcity.dao.NotificationRepository;
import com.example.smartcity.dao.UserRepository;
import com.example.smartcity.dao.VerificationTokenRepository;
import com.example.smartcity.dto.RegisterRequest;
import com.example.smartcity.metier.imp.AuthServiceImpl;
import com.example.smartcity.model.entity.Citoyen;
import com.example.smartcity.model.entity.Notification;
import com.example.smartcity.model.entity.User;
import com.example.smartcity.model.entity.VerificationToken;
import com.example.smartcity.model.enums.RoleUtilisateur;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthServiceImpl
 *
 * Tests the business logic of user registration and email verification without Spring context.
 * Focuses on: nominal case, edge cases, and error scenarios.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private VerificationTokenRepository tokenRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private String appUrl;

    @BeforeEach
    void setUp() {
        appUrl = "http://localhost:8080";

        registerRequest = new RegisterRequest();
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setNom("Mekni");
        registerRequest.setPrenom("Ali");
        registerRequest.setTelephone("50123456");
        registerRequest.setPassword("Password@123");
    }

    // ===== EDGE CASE 1: Email Already Exists =====

    @Test
    @DisplayName("Should reject registration when email already exists")
    void shouldRejectRegistration_WhenEmailAlreadyExists() {
        // GIVEN: Email already exists in database
        when(userRepository.existsByEmail(registerRequest.getEmail()))
                .thenReturn(true);

        // WHEN & THEN: Should throw exception
        assertThatThrownBy(() ->
                authService.registerCitoyen(registerRequest, appUrl)
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email déjà utilisé");

        // Verify no further processing occurred
        verify(passwordEncoder, never()).encode(any());
        verify(tokenRepository, never()).save(any(VerificationToken.class));
        verify(emailService, never()).send(any(), any(), any());
    }

    // ===== ERROR CASE 1: Invalid Email Format =====

//    @Test
//    @DisplayName("Should handle invalid email format gracefully")
//    void shouldHandleInvalidEmailFormat() {
//        // GIVEN: Invalid email format
//        registerRequest.setEmail("invalid-email-format");
//        when(userRepository.existsByEmail(registerRequest.getEmail()))
//                .thenReturn(false);
//        when(passwordEncoder.encode(registerRequest.getPassword()))
//                .thenReturn("encodedPassword123");
//        when(userRepository.save(any(Citoyen.class)))
//                .thenAnswer(invocation -> invocation.getArgument(0));
//        when(tokenRepository.save(any(VerificationToken.class)))
//                .thenAnswer(invocation -> invocation.getArgument(0));
//
//        // WHEN: Registering with invalid email
//        authService.registerCitoyen(registerRequest, appUrl);
//
//        // THEN: Should proceed (email validation happens at controller/validation layer)
//        verify(userRepository, times(2)).save(any(Citoyen.class));
//    }
//

    // ===== ERROR CASE 2: Invalid Token =====

    @Test
    @DisplayName("Should reject verification with invalid token")
    void shouldRejectVerification_WithInvalidToken() {
        // GIVEN: Invalid token not found in database
        when(tokenRepository.findByToken("invalid-token"))
                .thenReturn(Optional.empty());

        // WHEN & THEN: Should throw exception
        assertThatThrownBy(() ->
                authService.verifyEmail("invalid-token")
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Token invalide");

        // No user or token should be updated
        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).save(any());
    }

    // ===== ERROR CASE 3: Email Already Verified =====

    @Test
    @DisplayName("Should reject verification when email already confirmed")
    void shouldRejectVerification_WhenEmailAlreadyVerified() {
        // GIVEN: Token already confirmed
        String validToken = "already-confirmed-token";
        Citoyen citoyen = new Citoyen();
        citoyen.setEnabled(true);

        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(validToken);
        verificationToken.setUser(citoyen);
        verificationToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        verificationToken.setConfirmedAt(LocalDateTime.now().minusHours(1)); // Already confirmed

        when(tokenRepository.findByToken(validToken))
                .thenReturn(Optional.of(verificationToken));

        // WHEN & THEN: Should throw exception
        assertThatThrownBy(() ->
                authService.verifyEmail(validToken)
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email déjà vérifié");

        // No further updates should occur
        verify(userRepository, never()).save(any());
    }

    // ===== ERROR CASE 4: Token Expired =====

    @Test
    @DisplayName("Should reject verification when token has expired")
    void shouldRejectVerification_WhenTokenExpired() {
        // GIVEN: Expired verification token
        String expiredToken = "expired-token";
        Citoyen citoyen = new Citoyen();
        citoyen.setEnabled(false);

        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(expiredToken);
        verificationToken.setUser(citoyen);
        verificationToken.setExpiresAt(LocalDateTime.now().minusHours(1)); // Expired
        verificationToken.setConfirmedAt(null);

        when(tokenRepository.findByToken(expiredToken))
                .thenReturn(Optional.of(verificationToken));

        // WHEN & THEN: Should throw exception
        assertThatThrownBy(() ->
                authService.verifyEmail(expiredToken)
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Token expiré");

        // No user should be enabled
        verify(userRepository, never()).save(any());
    }

    // ===== PASSWORD ENCODING TEST =====

//    @Test
//    @DisplayName("Should properly encode password during registration")
//    void shouldEncodePassword_DuringRegistration() {
//        // GIVEN: Valid registration request
//        String plainPassword = "PlainPassword@123";
//        String encodedPassword = "encodedPassword$2a$10$xyz";
//
//        registerRequest.setPassword(plainPassword);
//
//        when(userRepository.existsByEmail(registerRequest.getEmail()))
//                .thenReturn(false);
//        when(passwordEncoder.encode(plainPassword))
//                .thenReturn(encodedPassword);
//        when(userRepository.save(any(Citoyen.class)))
//                .thenAnswer(invocation -> invocation.getArgument(0));
//        when(tokenRepository.save(any(VerificationToken.class)))
//                .thenAnswer(invocation -> invocation.getArgument(0));
//
//        // WHEN: Registering citizen
//        authService.registerCitoyen(registerRequest, appUrl);
//
//        // THEN: Password should be encoded
//        verify(passwordEncoder).encode(plainPassword);
//
//        ArgumentCaptor<Citoyen> citoyenCaptor = ArgumentCaptor.forClass(Citoyen.class);
//        verify(userRepository, times(2)).save(citoyenCaptor.capture());
//
//        Citoyen savedCitoyen = citoyenCaptor.getAllValues().get(0);
//        assertThat(savedCitoyen.getPassword()).isEqualTo(encodedPassword);
//    }

    // ===== TOKEN EXPIRY TEST =====

    @Test
    @DisplayName("Should set token expiry to 24 hours from creation")
    void shouldSetTokenExpiryTo24Hours() {
        // GIVEN: Valid registration request
        when(userRepository.existsByEmail(registerRequest.getEmail()))
                .thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword()))
                .thenReturn("encodedPassword123");
        when(userRepository.save(any(Citoyen.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenRepository.save(any(VerificationToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime beforeRegistration = LocalDateTime.now();

        // WHEN: Registering citizen
        authService.registerCitoyen(registerRequest, appUrl);

        // THEN: Token should expire in approximately 24 hours
        ArgumentCaptor<VerificationToken> tokenCaptor = ArgumentCaptor.forClass(VerificationToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());

        VerificationToken savedToken = tokenCaptor.getValue();
        LocalDateTime expectedExpiry = beforeRegistration.plusHours(24);

        assertThat(savedToken.getExpiresAt())
                .isCloseTo(expectedExpiry, within(1, java.time.temporal.ChronoUnit.MINUTES));
    }
}