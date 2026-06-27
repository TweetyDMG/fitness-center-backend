package com.fitnesscenter.repository;

import com.fitnesscenter.entity.Preference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PreferenceRepository extends JpaRepository<Preference, Long> {
    List<Preference> findByClientId(Long clientId);
}