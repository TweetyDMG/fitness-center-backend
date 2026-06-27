package com.fitnesscenter.repository;

import com.fitnesscenter.entity.PreferenceType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreferenceTypeRepository extends JpaRepository<PreferenceType, Long> {
}