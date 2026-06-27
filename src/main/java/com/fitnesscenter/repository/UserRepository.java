package com.fitnesscenter.repository;

import com.fitnesscenter.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.client WHERE u.username = :username")
    User findUserWithClientByUsername(String username);
}