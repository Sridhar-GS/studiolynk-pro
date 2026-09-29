package com.studiolynk.model.dto;

import java.util.ArrayList;
import java.util.List;

public class RatingSummaryDto {

    private Double averageRating;
    private Long totalRatings;
    private List<RatingDto> ratings = new ArrayList<>();

    public RatingSummaryDto() {
    }

    public RatingSummaryDto(Double averageRating, Long totalRatings, List<RatingDto> ratings) {
        this.averageRating = averageRating;
        this.totalRatings = totalRatings;
        this.ratings = ratings;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Long getTotalRatings() {
        return totalRatings;
    }

    public void setTotalRatings(Long totalRatings) {
        this.totalRatings = totalRatings;
    }

    public List<RatingDto> getRatings() {
        return ratings;
    }

    public void setRatings(List<RatingDto> ratings) {
        this.ratings = ratings;
    }
}
