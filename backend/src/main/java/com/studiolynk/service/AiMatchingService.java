package com.studiolynk.service;

import com.studiolynk.model.dto.FreelancerCardDto;
import com.studiolynk.model.dto.ml.MatchFeaturesDto;
import com.studiolynk.model.entity.Freelancer;
import com.studiolynk.model.entity.WorkRequirement;

import java.util.List;

/**
 * AI Matching and Ranking Service (DIS-002, DIS-006, ML-012, ML-013, WRK-005).
 * Coordinates feature extraction, hard filtering, ML prediction, and candidate ranking.
 */
public interface AiMatchingService {

    /**
     * Extracts exactly 6 deterministic features for a candidate against a requirement.
     * Ratings are strictly excluded (ML-005).
     */
    MatchFeaturesDto extractFeatures(WorkRequirement requirement, Freelancer freelancer);

    /**
     * Finds, hard-filters, scores, and ranks matching freelancers for a work requirement.
     * 1. Applies hard availability filters (ML-012).
     * 2. Extracts 6 normalized features for each eligible candidate.
     * 3. Vector-predicts scores with the DecisionTreeRegressor ML service (ML-010, ML-011).
     * 4. Sorts descending by match score and returns cards with aiMatchScore (DIS-002, DIS-006).
     */
    List<FreelancerCardDto> getMatchingFreelancersForRequirement(Long requirementId);

    /**
     * Scores and ranks candidate cards against a work requirement.
     */
    List<FreelancerCardDto> scoreAndRankCandidates(WorkRequirement requirement, List<FreelancerCardDto> candidateCards);
}
