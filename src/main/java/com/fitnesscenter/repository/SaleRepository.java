package com.fitnesscenter.repository;

import com.fitnesscenter.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SaleRepository extends JpaRepository<Sale, Long>, JpaSpecificationExecutor<Sale> {
    List<Sale> findByClientId(Long clientId);

    @Query("SELECT s FROM Sale s WHERE s.client.id = :clientId AND s.endDate >= CURRENT_DATE")
    Optional<Sale> findActiveSubscriptionByClientId(@Param("clientId") Long clientId);

    @Query("SELECT s FROM Sale s WHERE s.client.id = :clientId AND s.startDate <= :currentDate AND s.endDate >= :currentDate")
    Optional<Sale> findActiveSubscriptionByClientId(@Param("clientId") Long clientId, @Param("currentDate") LocalDate currentDate);

}