package com.example.smartcity.dto;

import com.example.smartcity.model.enums.Departement;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAgentRequest {

    @NotBlank @Size(max = 20)
    private String nom;

    @NotBlank @Size(max = 20)
    private String prenom;

    @NotBlank @Email @Size(max = 255)
    private String email;

    @Size(max = 15)
    private String telephone;

    @NotNull
    private Departement departement;


}
