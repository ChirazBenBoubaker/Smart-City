package com.example.smartcity.model.entity;

import com.example.smartcity.model.enums.Departement;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AgentMunicipal extends User {

    @Enumerated(EnumType.STRING)
    private Departement departement;

    private boolean enService = true;
    private int nombreIncidentsTraites = 0;
}

