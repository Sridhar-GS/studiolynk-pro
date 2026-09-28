package com.studiolynk.repository;

import com.studiolynk.model.entity.WorkRequest;
import com.studiolynk.model.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkRequestRepository extends JpaRepository<WorkRequest, Long> {

    Optional<WorkRequest> findByRequirementIdAndFreelancerId(Long requirementId, Long freelancerId);

    boolean existsByRequirementIdAndFreelancerId(Long requirementId, Long freelancerId);

    List<WorkRequest> findByRequirementId(Long requirementId);

    List<WorkRequest> findByRequirementIdAndStatus(Long requirementId, RequestStatus status);

    List<WorkRequest> findByFreelancerId(Long freelancerId);

    List<WorkRequest> findByFreelancerIdAndStatus(Long freelancerId, RequestStatus status);

    List<WorkRequest> findByRequirementStudioId(Long studioId);

    List<WorkRequest> findByRequirementStudioIdAndStatus(Long studioId, RequestStatus status);

    // WRK-008: Query other requests for a requirement when confirming one
    List<WorkRequest> findByRequirementIdAndIdNot(Long requirementId, Long requestId);

    // REQ-009: Conflict detection for double booking protection
    @Query("SELECT r FROM WorkRequest r " +
            "JOIN FETCH r.requirement req " +
            "WHERE r.freelancer.id = :freelancerId " +
            "AND r.status = :status " +
            "AND req.eventDate = :eventDate")
    List<WorkRequest> findByFreelancerIdAndStatusAndEventDate(
            @Param("freelancerId") Long freelancerId,
            @Param("status") RequestStatus status,
            @Param("eventDate") LocalDate eventDate
    );

    long countByRequirementIdAndStatus(Long requirementId, RequestStatus status);

    long countByFreelancerIdAndStatus(Long freelancerId, RequestStatus status);

    long countByRequirementStudioIdAndStatus(Long studioId, RequestStatus status);
}
