package com.example.smartcity.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "citoyens")
public class Citoyen extends User {
    // Tu peux ajouter des champs spécifiques citoyen plus tard (adresse, etc.)
}
