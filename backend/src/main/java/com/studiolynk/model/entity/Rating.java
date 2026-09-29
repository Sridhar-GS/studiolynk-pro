package com.studiolynk.model.entity;

import com.studiolynk.model.enums.RatingTargetType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Entity representing a two-way star rating and optional written review
 * between Studio and Freelancer for completed work (RAT-001 - RAT-006).
 */
@Entity
@Table(
    name = "ratings",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_rating_req_user", columnNames = {"requirement_id", "from_user_id"})
    }
)
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requirement_id", nullable = false)
    private WorkRequirement requirement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id", nullable = false)
    private User fromUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id", nullable = false)
    private User toUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private RatingTargetType targetType;

    @Column(name = "score", nullable = false)
    private Integer score;

    @Column(name = "review_text", columnDefinition = "TEXT")
    private String reviewText;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Rating() {
    }

    public Rating(WorkRequirement requirement, User fromUser, User toUser, RatingTargetType targetType, Integer score, String reviewText) {
        this.requirement = requirement;
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.targetType = targetType;
        this.score = score;
        this.reviewText = reviewText;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public WorkRequirement getRequirement() {
        return requirement;
    }

    public void setRequirement(WorkRequirement requirement) {
        this.requirement = requirement;
    }

    public User getFromUser() {
        return fromUser;
    }

    public void setFromUser(User fromUser) {
        this.fromUser = fromUser;
    }

    public User getToUser() {
        return toUser;
    }

    public void setToUser(User toUser) {
        this.toUser = toUser;
    }

    public RatingTargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(RatingTargetType targetType) {
        this.targetType = targetType;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
