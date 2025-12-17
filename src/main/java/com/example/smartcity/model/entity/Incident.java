package com.example.smartcity.model.entity;

import com.example.smartcity.model.enums.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;

    @Column(length = 1000)
    private String description;

    private Double latitude;
    private Double longitude;

    private LocalDateTime dateSignalement = LocalDateTime.now();
    private LocalDateTime datePriseEnCharge;
    private LocalDateTime dateResolution;
    private LocalDateTime dateCloture;

    @Enumerated(EnumType.STRING)
    private StatutIncident statut;

    @Enumerated(EnumType.STRING)
    private PrioriteIncident priorite;

    @Enumerated(EnumType.STRING)
    private Departement categorie;

    @ManyToOne
    @JoinColumn(name = "citoyen_id")
    private Citoyen citoyen;


    @ManyToOne
    private AgentMunicipal agentResponsable;

    @ManyToOne
    private Quartier quartier;

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Photo> photos = new ArrayList<>();

    @OneToOne(mappedBy = "incident", fetch = FetchType.LAZY, optional = true)
    private Feedback feedback;

}
