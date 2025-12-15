package com.example.smartcity.dao;
import com.example.smartcity.model.entity.Quartier;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface QuartierRepository extends JpaRepository<Quartier, Long> {
    Optional<Quartier> findByNomAndVille(String nom, String ville);
}
