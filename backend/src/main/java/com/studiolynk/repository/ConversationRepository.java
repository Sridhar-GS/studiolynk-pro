package com.studiolynk.repository;

import com.studiolynk.model.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByRequirementIdAndFreelancerId(Long requirementId, Long freelancerId);

    List<Conversation> findByStudioIdOrderByCreatedAtDesc(Long studioId);

    List<Conversation> findByFreelancerIdOrderByCreatedAtDesc(Long freelancerId);

    List<Conversation> findByRequirementId(Long requirementId);

    boolean existsByRequirementIdAndFreelancerId(Long requirementId, Long freelancerId);
}
