package com.studiolynk.service;

import com.studiolynk.model.dto.ml.CandidateBatchItemDto;
import com.studiolynk.model.dto.ml.MatchFeaturesDto;

import java.util.List;
import java.util.Map;

/**
 * Client interface for interacting with the Python FastAPI ML microservice.
 */
public interface MlServiceClient {

    /**
     * Batch predicts match scores for multiple candidates using vectorized prediction.
     * Returns a map of candidateId -> matchScore (0.0 to 100.0).
     */
    Map<Long, Double> predictBatch(List<CandidateBatchItemDto> candidates);

    /**
     * Predicts match score for a single candidate.
     */
    Double predictMatch(MatchFeaturesDto features);

    /**
     * Health check verifying if ML service is reachable and has model loaded.
     */
    boolean isAvailable();

    /**
     * Deterministic fallback scoring function using domain rules if ML microservice is unreachable.
     */
    Double computeFallbackScore(MatchFeaturesDto features);
}
