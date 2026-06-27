package com.fitnesscenter.repository;

import com.fitnesscenter.entity.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerRepository extends JpaRepository <Trainer, Long> {
}
