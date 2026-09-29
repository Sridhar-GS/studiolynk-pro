package com.studiolynk.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RatingSubmissionDto {

    @NotNull(message = "Rating score is required")
    @Min(value = 1, message = "Rating score must be between 1 and 5")
    @Max(value = 5, message = "Rating score must be between 1 and 5")
    private Integer score;

    @Size(max = 2000, message = "Review text cannot exceed 2000 characters")
    private String reviewText;

    public RatingSubmissionDto() {
    }

    public RatingSubmissionDto(Integer score, String reviewText) {
        this.score = score;
        this.reviewText = reviewText;
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
}
