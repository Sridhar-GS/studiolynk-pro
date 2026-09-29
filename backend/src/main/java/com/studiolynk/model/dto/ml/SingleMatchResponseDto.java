package com.studiolynk.model.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SingleMatchResponseDto {

    @JsonProperty("matchScore")
    private Double matchScore;

    @JsonProperty("status")
    private String status;

    public SingleMatchResponseDto() {
    }

    public SingleMatchResponseDto(Double matchScore, String status) {
        this.matchScore = matchScore;
        this.status = status;
    }

    public Double getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(Double matchScore) {
        this.matchScore = matchScore;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
