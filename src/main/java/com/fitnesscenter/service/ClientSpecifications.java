package com.fitnesscenter.service;

import com.fitnesscenter.dto.ClientFilterRequest;
import com.fitnesscenter.entity.Client;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Component
public class ClientSpecifications {

    public static Specification<Client> withFilter(ClientFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.getSearchQuery() != null && !filter.getSearchQuery().trim().isEmpty()) {
                String searchPattern = "%" + filter.getSearchQuery().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("firstname")), searchPattern),
                        cb.like(cb.lower(root.get("lastname")), searchPattern),
                        cb.like(root.get("phone"), searchPattern),
                        cb.like(cb.lower(root.get("email")), searchPattern)
                ));
            }
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}