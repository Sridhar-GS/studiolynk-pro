package com.studiolynk.repository;

import com.studiolynk.model.entity.PortfolioImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioImageRepository extends JpaRepository<PortfolioImage, Long> {
    List<PortfolioImage> findByCategoryIdOrderBySortOrderAsc(Long categoryId);
    Optional<PortfolioImage> findByIdAndCategoryPortfolioFreelancerUserEmail(Long id, String userEmail);
}
