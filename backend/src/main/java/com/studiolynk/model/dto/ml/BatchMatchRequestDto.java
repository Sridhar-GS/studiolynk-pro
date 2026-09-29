package com.studiolynk.model.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class BatchMatchRequestDto {

    @JsonProperty("candidates")
    private List<CandidateBatchItemDto> candidates = new ArrayList<>();

    public BatchMatchRequestDto() {
    }

    public BatchMatchRequestDto(List<CandidateBatchItemDto> candidates) {
        this.candidates = candidates;
    }

    public List<CandidateBatchItemDto> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<CandidateBatchItemDto> candidates) {
        this.candidates = candidates;
    }
}
