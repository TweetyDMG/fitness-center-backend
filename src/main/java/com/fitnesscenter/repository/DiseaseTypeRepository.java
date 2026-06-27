package com.fitnesscenter.repository;

import com.fitnesscenter.entity.DiseaseType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiseaseTypeRepository extends JpaRepository<DiseaseType, Long> {

}