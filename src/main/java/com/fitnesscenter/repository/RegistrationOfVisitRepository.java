package com.fitnesscenter.repository;

import com.fitnesscenter.entity.RegistrationOfVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RegistrationOfVisitRepository extends JpaRepository<RegistrationOfVisit, Long> {
    @Query("SELECT COUNT(v) FROM RegistrationOfVisit v WHERE v.saleId = :saleId")
    int countBySaleId(@Param("saleId") Long saleId);

    @Query("SELECT v FROM RegistrationOfVisit v WHERE v.saleId IN (SELECT s.id FROM Sale s WHERE s.client.id = :clientId)")
    List<RegistrationOfVisit> findByClientId(@Param("clientId") Long clientId);

    List<RegistrationOfVisit> findByScheduleId(Long scheduleId);

    List<RegistrationOfVisit> findBySaleIdIn(List<Long> saleIds);

    boolean existsByScheduleId(Long scheduleId);

    boolean existsByScheduleIdAndSaleIdIn(Long scheduleId, List<Long> saleIds);

}