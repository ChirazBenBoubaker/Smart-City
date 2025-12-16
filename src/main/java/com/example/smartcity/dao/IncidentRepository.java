package com.example.smartcity.dao;

import com.example.smartcity.model.entity.Citoyen;
import com.example.smartcity.model.entity.Incident;
import com.example.smartcity.model.enums.StatutIncident;
import com.example.smartcity.model.enums.Departement;
import com.example.smartcity.model.enums.PrioriteIncident;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    // Méthodes existantes...
    Page<Incident> findByCitoyen(Citoyen citoyen, Pageable pageable);
    long countByCitoyenAndStatut(Citoyen citoyen, StatutIncident statut);
    long countByCitoyen(Citoyen citoyen);
    List<Incident> findTop5ByCitoyenOrderByDateSignalementDesc(Citoyen citoyen);

    @Nullable Object countByStatutNot(StatutIncident statut);
    @Query("""
        SELECT FUNCTION('DATE', i.dateSignalement), COUNT(i)
        FROM Incident i
        WHERE i.dateSignalement >= :startDate
        GROUP BY FUNCTION('DATE', i.dateSignalement)
        ORDER BY FUNCTION('DATE', i.dateSignalement)
    """)
    List<Object[]> countIncidentsByDay(@Param("startDate") LocalDateTime startDate);


    // ===== NOUVELLES MÉTHODES POUR FILTRAGE AVANCÉ PAR QUARTIER =====

    /**
     * Récupère les villes distinctes des incidents d'un citoyen
     */
    @Query("SELECT DISTINCT i.quartier.ville FROM Incident i " +
            "WHERE i.citoyen = :citoyen " +
            "AND i.quartier IS NOT NULL " +
            "AND i.quartier.ville IS NOT NULL " +
            "ORDER BY i.quartier.ville")
    List<String> findDistinctVillesByCitoyen(@Param("citoyen") Citoyen citoyen);

    /**
     * Récupère les gouvernorats distincts des incidents d'un citoyen
     */
    @Query("SELECT DISTINCT i.quartier.gouvernorat FROM Incident i " +
            "WHERE i.citoyen = :citoyen " +
            "AND i.quartier IS NOT NULL " +
            "AND i.quartier.gouvernorat IS NOT NULL " +
            "ORDER BY i.quartier.gouvernorat")
    List<String> findDistinctGouvernoratsByCitoyen(@Param("citoyen") Citoyen citoyen);

    /**
     * Récupère les quartiers distincts des incidents d'un citoyen
     * Format: "Nom du quartier (Ville, Gouvernorat)"
     */
    @Query("SELECT DISTINCT CONCAT(i.quartier.nom, ' (', i.quartier.ville, ', ', i.quartier.gouvernorat, ')') " +
            "FROM Incident i " +
            "WHERE i.citoyen = :citoyen " +
            "AND i.quartier IS NOT NULL ")
    List<String> findDistinctQuartiersCompletsByCitoyen(@Param("citoyen") Citoyen citoyen);

    /**
     * Récupère les quartiers par ville pour un citoyen
     */
    @Query("SELECT DISTINCT i.quartier.nom FROM Incident i " +
            "WHERE i.citoyen = :citoyen " +
            "AND i.quartier.ville = :ville " +
            "AND i.quartier IS NOT NULL " +
            "ORDER BY i.quartier.nom")
    List<String> findQuartiersByVilleForCitoyen(
            @Param("citoyen") Citoyen citoyen,
            @Param("ville") String ville
    );

    /**
     * Recherche multi-critères avec filtres incluant ville et gouvernorat
     */
    @Query("SELECT i FROM Incident i " +
            "WHERE i.citoyen = :citoyen " +
            "AND (:statut IS NULL OR i.statut = :statut) " +
            "AND (:categorie IS NULL OR i.categorie = :categorie) " +
            "AND (:priorite IS NULL OR i.priorite = :priorite) " +
            "AND (:quartier IS NULL OR i.quartier.nom = :quartier) " +
            "AND (:ville IS NULL OR i.quartier.ville = :ville) " +
            "AND (:gouvernorat IS NULL OR i.quartier.gouvernorat = :gouvernorat) " +
            "AND (:recherche IS NULL OR :recherche = '' OR " +
            "     LOWER(i.titre) LIKE LOWER(CONCAT('%', :recherche, '%')) OR " +
            "     LOWER(i.description) LIKE LOWER(CONCAT('%', :recherche, '%')))")


    Page<Incident> findByCitoyenWithFiltersAvances(
            @Param("citoyen") Citoyen citoyen,
            @Param("statut") StatutIncident statut,
            @Param("categorie") Departement categorie,
            @Param("priorite") PrioriteIncident priorite,
            @Param("quartier") String quartier,
            @Param("ville") String ville,
            @Param("gouvernorat") String gouvernorat,
            @Param("recherche") String recherche,
            Pageable pageable
    );

    @Query("""
        SELECT DISTINCT i.quartier.nom
        FROM Incident i
        WHERE i.citoyen = :citoyen
        AND i.quartier IS NOT NULL
        ORDER BY i.quartier.nom
        """)
    List<String> findDistinctQuartierNomsByCitoyen(@Param("citoyen") Citoyen citoyen);
}
