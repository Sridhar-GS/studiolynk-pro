package com.studiolynk.model.dto;

import com.studiolynk.model.enums.RatingTargetType;
import java.time.Instant;

public class RatingDto {

    private Long id;
    private Long requirementId;
    private String requirementTitle;
    private Long fromUserId;
    private String fromUserName;
    private String fromUserRole;
    private Long toUserId;
    private String toUserName;
    private RatingTargetType targetType;
    private Integer score;
    private String reviewText;
    private Instant createdAt;

    public RatingDto() {
    }

    public RatingDto(Long id, Long requirementId, String requirementTitle, Long fromUserId,
                     String fromUserName, String fromUserRole, Long toUserId, String toUserName,
                     RatingTargetType targetType, Integer score, String reviewText, Instant createdAt) {
        this.id = id;
        this.requirementId = requirementId;
        this.requirementTitle = requirementTitle;
        this.fromUserId = fromUserId;
        this.fromUserName = fromUserName;
        this.fromUserRole = fromUserRole;
        this.toUserId = toUserId;
        this.toUserName = toUserName;
        this.targetType = targetType;
        this.score = score;
        this.reviewText = reviewText;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRequirementId() {
        return requirementId;
    }

    public void setRequirementId(Long requirementId) {
        this.requirementId = requirementId;
    }

    public String getRequirementTitle() {
        return requirementTitle;
    }

    public void setRequirementTitle(String requirementTitle) {
        this.requirementTitle = requirementTitle;
    }

    public Long getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(Long fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getFromUserName() {
        return fromUserName;
    }

    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }

    public String getFromUserRole() {
        return fromUserRole;
    }

    public void setFromUserRole(String fromUserRole) {
        this.fromUserRole = fromUserRole;
    }

    public Long getToUserId() {
        return toUserId;
    }

    public void setToUserId(Long toUserId) {
        this.toUserId = toUserId;
    }

    public String getToUserName() {
        return toUserName;
    }

    public void setToUserName(String toUserName) {
        this.toUserName = toUserName;
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
