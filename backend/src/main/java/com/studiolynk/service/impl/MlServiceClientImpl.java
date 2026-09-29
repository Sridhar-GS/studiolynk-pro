package com.studiolynk.service.impl;

import com.studiolynk.model.dto.ml.BatchMatchRequestDto;
import com.studiolynk.model.dto.ml.BatchMatchResponseDto;
import com.studiolynk.model.dto.ml.CandidateBatchItemDto;
import com.studiolynk.model.dto.ml.CandidateScoreResponseDto;
import com.studiolynk.model.dto.ml.MatchFeaturesDto;
import com.studiolynk.model.dto.ml.SingleMatchResponseDto;
import com.studiolynk.service.MlServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MlServiceClientImpl implements MlServiceClient {

    private static final Logger log = LoggerFactory.getLogger(MlServiceClientImpl.class);

    private final RestClient restClient;
    private final String mlServiceUrl;

    public MlServiceClientImpl(
            @Value("${ml.service.url:http://127.0.0.1:8000}") String mlServiceUrl,
            @Value("${ml.service.connect-timeout-ms:2000}") int connectTimeoutMs,
            @Value("${ml.service.read-timeout-ms:3000}") int readTimeoutMs) {
        this.mlServiceUrl = mlServiceUrl;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);

        this.restClient = RestClient.builder()
                .baseUrl(mlServiceUrl)
                .requestFactory(factory)
                .build();
    }

    @Override
    public Map<Long, Double> predictBatch(List<CandidateBatchItemDto> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            BatchMatchRequestDto request = new BatchMatchRequestDto(candidates);
            BatchMatchResponseDto response = restClient.post()
                    .uri("/predict-batch")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(BatchMatchResponseDto.class);

            if (response != null && response.getPredictions() != null) {
                Map<Long, Double> scoreMap = new HashMap<>();
                for (CandidateScoreResponseDto item : response.getPredictions()) {
                    if (item.getCandidateId() != null && item.getMatchScore() != null) {
                        scoreMap.put(item.getCandidateId(), item.getMatchScore());
                    }
                }
                return scoreMap;
            }
        } catch (Exception e) {
            log.warn("Failed to reach ML microservice at {} for batch prediction: {}. Falling back to deterministic scoring.",
                    mlServiceUrl, e.getMessage());
        }

        // Fallback: Compute deterministic scores using domain logic
        Map<Long, Double> fallbackMap = new HashMap<>();
        for (CandidateBatchItemDto item : candidates) {
            Double fallbackScore = computeFallbackScore(item.getFeatures());
            fallbackMap.put(item.getCandidateId(), fallbackScore);
        }
        return fallbackMap;
    }

    @Override
    public Double predictMatch(MatchFeaturesDto features) {
        if (features == null) {
            return 50.0;
        }

        try {
            SingleMatchResponseDto response = restClient.post()
                    .uri("/predict-match")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(features)
                    .retrieve()
                    .body(SingleMatchResponseDto.class);

            if (response != null && response.getMatchScore() != null) {
                return response.getMatchScore();
            }
        } catch (Exception e) {
            log.warn("Failed to reach ML microservice at {} for single prediction: {}. Using fallback.",
                    mlServiceUrl, e.getMessage());
        }

        return computeFallbackScore(features);
    }

    @Override
    public boolean isAvailable() {
        try {
            String res = restClient.get()
                    .uri("/health")
                    .retrieve()
                    .body(String.class);
            return res != null && res.contains("UP");
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Double computeFallbackScore(MatchFeaturesDto features) {
        if (features == null) {
            return 50.0;
        }

        double skill = features.getSkillMatch() != null ? features.getSkillMatch() : 0.5;
        double portfolio = features.getPortfolioRelevance() != null ? features.getPortfolioRelevance() : 0.5;
        double experience = features.getExperience() != null ? features.getExperience() : 0.5;
        double budget = features.getBudgetCompatibility() != null ? features.getBudgetCompatibility() : 0.5;
        double distance = features.getLocationDistance() != null ? features.getLocationDistance() : 0.5;
        double avail = features.getAvailabilityTimeCompatibility() != null ? features.getAvailabilityTimeCompatibility() : 1.0;

        // Base linear score (0 - 100)
        double score = 100.0 * (
                0.30 * skill
                        + 0.25 * portfolio
                        + 0.15 * budget
                        + 0.15 * avail
                        + 0.10 * distance
                        + 0.05 * experience
        );

        // Technical capability penalty
        if (skill < 0.25) {
            score *= (0.40 + 0.60 * (skill / 0.25));
        }

        // Availability clash penalty
        if (avail < 0.15) {
            score *= (0.50 + 0.50 * (avail / 0.15));
        }

        // Budget mismatch friction
        if (budget < 0.20) {
            score *= 0.90;
        }

        // Synergy bonus
        if (skill >= 0.85 && portfolio >= 0.85) {
            score += 3.0;
        }

        double bounded = Math.max(0.0, Math.min(100.0, score));
        return Math.round(bounded * 10.0) / 10.0;
    }
}
