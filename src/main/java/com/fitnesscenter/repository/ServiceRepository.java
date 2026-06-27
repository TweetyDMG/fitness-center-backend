package com.fitnesscenter.repository;

import com.fitnesscenter.entity.FitnessService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends JpaRepository<FitnessService, Long> {
}
