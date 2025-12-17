package com.example.smartcity.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "feedbacks")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Note de satisfaction (1 à 5 étoiles)
     */
    @Column(nullable = false)
    private Integer note;

    /**
     * Commentaire du citoyen (optionnel)
     */
    @Column(length = 1000)
    private String commentaire;

    /**
     * Date de création du feedback
     */
    @Column(nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    /**
     * Incident concerné par le feedback (relation One-to-One)
     */
    @OneToOne
    @JoinColumn(name = "incident_id", nullable = false, unique = true)
    private Incident incident;

    /**
     * Citoyen qui a donné le feedback
     */
    @ManyToOne
    @JoinColumn(name = "citoyen_id", nullable = false)
    private Citoyen citoyen;

}