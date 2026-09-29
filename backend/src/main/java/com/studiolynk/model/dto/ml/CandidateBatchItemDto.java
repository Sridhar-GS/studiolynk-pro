package com.studiolynk.model.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CandidateBatchItemDto {

    @JsonProperty("candidateId")
    private Long candidateId;

    @JsonProperty("features")
    private MatchFeaturesDto features;

    public CandidateBatchItemDto() {
    }

    public CandidateBatchItemDto(Long candidateId, MatchFeaturesDto features) {
        this.candidateId = candidateId;
        this.features = features;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }

    public MatchFeaturesDto getFeatures() {
        return features;
    }

    public void setFeatures(MatchFeaturesDto features) {
        this.features = features;
    }
}
