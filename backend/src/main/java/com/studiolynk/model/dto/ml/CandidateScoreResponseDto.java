package com.studiolynk.model.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CandidateScoreResponseDto {

    @JsonProperty("candidateId")
    private Long candidateId;

    @JsonProperty("matchScore")
    private Double matchScore;

    public CandidateScoreResponseDto() {
    }

    public CandidateScoreResponseDto(Long candidateId, Double matchScore) {
        this.candidateId = candidateId;
        this.matchScore = matchScore;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }

    public Double getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(Double matchScore) {
        this.matchScore = matchScore;
    }
}
