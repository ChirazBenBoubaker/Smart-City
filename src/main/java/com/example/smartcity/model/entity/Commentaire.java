package com.example.smartcity.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Commentaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500)
    private String contenu;

    private LocalDateTime dateCreation = LocalDateTime.now();

    @ManyToOne
    private User auteur;

    @ManyToOne
    private Incident incident;
}