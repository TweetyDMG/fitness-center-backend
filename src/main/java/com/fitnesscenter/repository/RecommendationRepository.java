package com.fitnesscenter.repository;

import com.fitnesscenter.entity.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
    List<Recommendation> findByClientId(Long clientId);

    List<Recommendation> findByTrainerId(Long trainerId);
}
