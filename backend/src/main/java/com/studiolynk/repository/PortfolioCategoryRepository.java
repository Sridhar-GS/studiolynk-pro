package com.studiolynk.repository;

import com.studiolynk.model.entity.PortfolioCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioCategoryRepository extends JpaRepository<PortfolioCategory, Long> {
    List<PortfolioCategory> findByPortfolioIdOrderBySortOrderAsc(Long portfolioId);
    Optional<PortfolioCategory> findByIdAndPortfolioFreelancerUserEmail(Long id, String userEmail);
}
