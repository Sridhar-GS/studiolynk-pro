package com.studiolynk.repository;

import com.studiolynk.model.entity.WorkRequirement;
import com.studiolynk.model.enums.RequirementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkRequirementRepository extends JpaRepository<WorkRequirement, Long> {

    List<WorkRequirement> findByStudioIdOrderByCreatedAtDesc(Long studioId);

    List<WorkRequirement> findByStudioIdAndStatusOrderByCreatedAtDesc(Long studioId, RequirementStatus status);

    Optional<WorkRequirement> findByIdAndStudioId(Long id, Long studioId);

    List<WorkRequirement> findByStatusOrderByEventDateAsc(RequirementStatus status);

    @Query("SELECT r FROM WorkRequirement r WHERE r.status = :status AND r.eventDate >= :fromDate ORDER BY r.eventDate ASC")
    List<WorkRequirement> findActiveOpenRequirements(
            @Param("status") RequirementStatus status,
            @Param("fromDate") LocalDate fromDate);

    long countByStudioId(Long studioId);

    long countByStudioIdAndStatus(Long studioId, RequirementStatus status);
}
