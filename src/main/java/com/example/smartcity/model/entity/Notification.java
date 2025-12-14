package com.example.smartcity.model.entity;


import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;
    private String message;
    private LocalDateTime dateEnvoi = LocalDateTime.now();
    private boolean lu = false;

    @ManyToOne
    private User utilisateur;

    @ManyToOne
    private Incident incident;
}
