package com.fitnesscenter.repository;

import com.fitnesscenter.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByTrainer(Long trainerId);

    @Query("SELECT s FROM Schedule s WHERE s.trainer = :trainerId AND s.date >= CURRENT_DATE ORDER BY s.date, s.startTime")
    List<Schedule> findFutureSlotsByTrainerId(@Param("trainerId") Long trainerId);

    List<Schedule> findByDate(LocalDate date);
}
