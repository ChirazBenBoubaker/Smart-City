package com.example.smartcity.model.entity;

import com.example.smartcity.model.enums.RoleUtilisateur;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String prenom;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;
    private String telephone;

    @Enumerated(EnumType.STRING)
    private RoleUtilisateur role;

    private boolean enabled = true;
    private boolean accountNonLocked = true;

    private LocalDateTime dateInscription = LocalDateTime.now();
}
