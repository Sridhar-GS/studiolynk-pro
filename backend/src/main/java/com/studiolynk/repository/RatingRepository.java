package com.studiolynk.repository;

import com.studiolynk.model.entity.Rating;
import com.studiolynk.model.enums.RatingTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    boolean existsByRequirementIdAndFromUserId(Long requirementId, Long fromUserId);

    Optional<Rating> findByRequirementIdAndFromUserId(Long requirementId, Long fromUserId);

    List<Rating> findByRequirementId(Long requirementId);

    List<Rating> findByToUserIdAndTargetTypeOrderByCreatedAtDesc(Long toUserId, RatingTargetType targetType);

    @Query("SELECT COALESCE(AVG(r.score), 0.0) FROM Rating r WHERE r.toUser.id = :toUserId AND r.targetType = :targetType")
    Double findAverageScoreByToUserIdAndTargetType(@Param("toUserId") Long toUserId, @Param("targetType") RatingTargetType targetType);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.toUser.id = :toUserId AND r.targetType = :targetType")
    Long countByToUserIdAndTargetType(@Param("toUserId") Long toUserId, @Param("targetType") RatingTargetType targetType);
}
