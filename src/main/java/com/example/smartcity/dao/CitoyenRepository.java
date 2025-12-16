package com.example.smartcity.dao;

import com.example.smartcity.model.entity.Citoyen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CitoyenRepository extends JpaRepository<Citoyen, Long> {
    Optional<Citoyen> findByEmail(String email);
    long countByEnabledTrue();
}
