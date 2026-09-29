package com.studiolynk.model.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 6 Normalized features for DecisionTreeRegressor match prediction.
 * Values are strictly bounded between 0.0 and 1.0.
 * Ratings are strictly excluded (ML-005).
 */
public class MatchFeaturesDto {

    @JsonProperty("skill_match")
    private Double skillMatch;

    @JsonProperty("portfolio_relevance")
    private Double portfolioRelevance;

    @JsonProperty("experience")
    private Double experience;

    @JsonProperty("budget_compatibility")
    private Double budgetCompatibility;

    @JsonProperty("location_distance")
    private Double locationDistance;

    @JsonProperty("availability_time_compatibility")
    private Double availabilityTimeCompatibility;

    public MatchFeaturesDto() {
    }

    public MatchFeaturesDto(Double skillMatch, Double portfolioRelevance, Double experience,
                            Double budgetCompatibility, Double locationDistance, Double availabilityTimeCompatibility) {
        this.skillMatch = skillMatch;
        this.portfolioRelevance = portfolioRelevance;
        this.experience = experience;
        this.budgetCompatibility = budgetCompatibility;
        this.locationDistance = locationDistance;
        this.availabilityTimeCompatibility = availabilityTimeCompatibility;
    }

    public Double getSkillMatch() {
        return skillMatch;
    }

    public void setSkillMatch(Double skillMatch) {
        this.skillMatch = skillMatch;
    }

    public Double getPortfolioRelevance() {
        return portfolioRelevance;
    }

    public void setPortfolioRelevance(Double portfolioRelevance) {
        this.portfolioRelevance = portfolioRelevance;
    }

    public Double getExperience() {
        return experience;
    }

    public void setExperience(Double experience) {
        this.experience = experience;
    }

    public Double getBudgetCompatibility() {
        return budgetCompatibility;
    }

    public void setBudgetCompatibility(Double budgetCompatibility) {
        this.budgetCompatibility = budgetCompatibility;
    }

    public Double getLocationDistance() {
        return locationDistance;
    }

    public void setLocationDistance(Double locationDistance) {
        this.locationDistance = locationDistance;
    }

    public Double getAvailabilityTimeCompatibility() {
        return availabilityTimeCompatibility;
    }

    public void setAvailabilityTimeCompatibility(Double availabilityTimeCompatibility) {
        this.availabilityTimeCompatibility = availabilityTimeCompatibility;
    }

    @Override
    public String toString() {
        return "MatchFeaturesDto{" +
                "skillMatch=" + skillMatch +
                ", portfolioRelevance=" + portfolioRelevance +
                ", experience=" + experience +
                ", budgetCompatibility=" + budgetCompatibility +
                ", locationDistance=" + locationDistance +
                ", availabilityTimeCompatibility=" + availabilityTimeCompatibility +
                '}';
    }
}
