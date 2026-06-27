package com.fitnesscenter.repository;

import com.fitnesscenter.entity.Disease;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiseaseRepository extends JpaRepository<Disease, Long> {
    List<Disease> findByClientId(Long clientId);
}