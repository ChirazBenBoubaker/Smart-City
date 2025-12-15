package com.example.smartcity.model.entity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomFichier;
    private String chemin;
    private String type;
    private Long taille;

    @ManyToOne
    @JoinColumn(name = "incident_id")
    private Incident incident;
}