package com.fitnesscenter.service;

import com.fitnesscenter.dto.SaleFilterRequest;
import com.fitnesscenter.entity.Sale;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class SaleSpecifications {

    public static Specification<Sale> withFilter(SaleFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getSearchTerm() != null && !filter.getSearchTerm().trim().isEmpty()) {
                String searchPattern = "%" + filter.getSearchTerm().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("client").get("firstname")), searchPattern),
                        cb.like(cb.lower(root.get("client").get("lastname")), searchPattern),
                        cb.like(root.get("id").as(String.class), searchPattern)
                ));
            }
            if (filter.getClientId() != null) {
                predicates.add(cb.equal(root.get("client").get("id"), filter.getClientId()));
            }
            if (filter.getFitnessCenterId() != null) {
                predicates.add(cb.equal(root.get("fitnessCenter").get("id"), filter.getFitnessCenterId()));
            }
            if (filter.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), filter.getFromDate()));
            }
            if (filter.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("startDate"), filter.getToDate()));
            }
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}