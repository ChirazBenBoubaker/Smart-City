package com.example.smartcity.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Rapport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dateGeneration;

    @Lob
    private byte[] donnees;

    private String typeRapport;

    @ManyToOne
    private User generePar;
}
