package com.studiolynk.repository;

import com.studiolynk.model.entity.StudioIdentitySubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudioIdentitySubmissionRepository extends JpaRepository<StudioIdentitySubmission, Long> {
    List<StudioIdentitySubmission> findByStudioId(Long studioId);
    Optional<StudioIdentitySubmission> findTopByStudioIdOrderBySubmittedAtDesc(Long studioId);
    void deleteByStudioId(Long studioId);
}
