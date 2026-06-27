package com.fitnesscenter.repository;

import com.fitnesscenter.entity.FitnessCenter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FitnessCenterRepository extends JpaRepository<FitnessCenter, Long> {
}