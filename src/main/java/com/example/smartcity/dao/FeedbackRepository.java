package com.example.smartcity.dao;

import com.example.smartcity.model.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    Optional<Feedback> findByIncidentId(Long incidentId);

    boolean existsByIncidentId(Long incidentId);
}