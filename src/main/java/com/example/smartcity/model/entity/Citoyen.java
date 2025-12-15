package com.example.smartcity.model.entity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "citoyens")
@PrimaryKeyJoinColumn(name = "id")
public class Citoyen extends User{

}
