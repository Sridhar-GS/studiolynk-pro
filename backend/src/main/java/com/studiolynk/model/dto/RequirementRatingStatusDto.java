package com.studiolynk.model.dto;

public class RequirementRatingStatusDto {

    private Long requirementId;
    private String requirementStatus;
    private boolean canRate;
    private boolean alreadyRated;
    private RatingDto myRating;
    private boolean counterpartyRated;
    private RatingDto counterpartyRating;

    public RequirementRatingStatusDto() {
    }

    public RequirementRatingStatusDto(Long requirementId, String requirementStatus, boolean canRate,
                                      boolean alreadyRated, RatingDto myRating,
                                      boolean counterpartyRated, RatingDto counterpartyRating) {
        this.requirementId = requirementId;
        this.requirementStatus = requirementStatus;
        this.canRate = canRate;
        this.alreadyRated = alreadyRated;
        this.myRating = myRating;
        this.counterpartyRated = counterpartyRated;
        this.counterpartyRating = counterpartyRating;
    }

    public Long getRequirementId() {
        return requirementId;
    }

    public void setRequirementId(Long requirementId) {
        this.requirementId = requirementId;
    }

    public String getRequirementStatus() {
        return requirementStatus;
    }

    public void setRequirementStatus(String requirementStatus) {
        this.requirementStatus = requirementStatus;
    }

    public boolean isCanRate() {
        return canRate;
    }

    public void setCanRate(boolean canRate) {
        this.canRate = canRate;
    }

    public boolean isAlreadyRated() {
        return alreadyRated;
    }

    public void setAlreadyRated(boolean alreadyRated) {
        this.alreadyRated = alreadyRated;
    }

    public RatingDto getMyRating() {
        return myRating;
    }

    public void setMyRating(RatingDto myRating) {
        this.myRating = myRating;
    }

    public boolean isCounterpartyRated() {
        return counterpartyRated;
    }

    public void setCounterpartyRated(boolean counterpartyRated) {
        this.counterpartyRated = counterpartyRated;
    }

    public RatingDto getCounterpartyRating() {
        return counterpartyRating;
    }

    public void setCounterpartyRating(RatingDto counterpartyRating) {
        this.counterpartyRating = counterpartyRating;
    }
}
