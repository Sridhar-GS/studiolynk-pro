package com.studiolynk.repository;

import com.studiolynk.model.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    Optional<Portfolio> findByFreelancerId(Long freelancerId);
    Optional<Portfolio> findByFreelancerUserId(Long userId);
    Optional<Portfolio> findByFreelancerUserEmail(String userEmail);
}
