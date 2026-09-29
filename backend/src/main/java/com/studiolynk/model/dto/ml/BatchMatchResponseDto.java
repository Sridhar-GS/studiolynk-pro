package com.studiolynk.model.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class BatchMatchResponseDto {

    @JsonProperty("predictions")
    private List<CandidateScoreResponseDto> predictions = new ArrayList<>();

    @JsonProperty("total")
    private Integer total;

    @JsonProperty("status")
    private String status;

    public BatchMatchResponseDto() {
    }

    public List<CandidateScoreResponseDto> getPredictions() {
        return predictions;
    }

    public void setPredictions(List<CandidateScoreResponseDto> predictions) {
        this.predictions = predictions;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
