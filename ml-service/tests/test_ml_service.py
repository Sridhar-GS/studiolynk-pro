"""
Unit and Integration Tests for StudioLynk Machine Learning Microservice.

Covers:
- ML-001: Python, FastAPI, pandas, NumPy, scikit-learn stack
- ML-002: Model architecture (DecisionTreeRegressor)
- ML-003: 5,000 synthetic records
- ML-004: Exactly 6 features in verified order
- ML-005: Strict exclusion of ratings
- ML-006: Domain logic and boundary conditions
- ML-007: Train/test split and cross-validation
- ML-008: Evaluation metrics (MAE, RMSE, R2)
- ML-009: Tree hyperparameter tuning
- ML-010: FastAPI prediction endpoints
- ML-011: 0-100 match score output
"""

import json
import os
import sys
from pathlib import Path
import pytest
import pandas as pd
import numpy as np
from sklearn.tree import DecisionTreeRegressor
from fastapi.testclient import TestClient

# Ensure ml-service root is in sys.path
TESTS_DIR = Path(__file__).resolve().parent
ROOT_DIR = TESTS_DIR.parent
if str(ROOT_DIR) not in sys.path:
    sys.path.insert(0, str(ROOT_DIR))

from app.dataset import (
    FEATURE_NAMES,
    TARGET_NAME,
    generate_synthetic_dataset,
    compute_match_score,
)
from app.predictor import ModelPredictor
from app.main import app
from app.schemas import MatchFeatures, CandidateFeatureItem


client = TestClient(app)


class TestSyntheticDataset:
    """Tests synthetic dataset generation and domain rules (ML-003, ML-004, ML-005, ML-006)."""

    def test_dataset_shape_and_features(self):
        df = generate_synthetic_dataset(n_samples=5000, random_state=42)
        assert len(df) == 5000, "Dataset must contain exactly 5,000 synthetic records (ML-003)"

        # Exactly 6 features plus target
        expected_columns = set(FEATURE_NAMES + [TARGET_NAME])
        assert set(df.columns) == expected_columns, f"Columns mismatch. Expected {expected_columns}"
        assert len(FEATURE_NAMES) == 6, "Must define exactly 6 features (ML-004)"

        # Ratings MUST NOT be in columns (ML-005)
        assert "rating" not in df.columns
        assert "reviews" not in df.columns
        assert "average_rating" not in df.columns

    def test_feature_and_target_bounds(self):
        df = generate_synthetic_dataset(n_samples=1000, random_state=123)

        # All features must strictly lie between 0.0 and 1.0
        for feat in FEATURE_NAMES:
            assert df[feat].min() >= 0.0, f"Feature {feat} contains values below 0.0"
            assert df[feat].max() <= 1.0, f"Feature {feat} contains values above 1.0"

        # Target match score must lie between 0.0 and 100.0 (ML-011)
        assert df[TARGET_NAME].min() >= 0.0, "Match score cannot be below 0.0"
        assert df[TARGET_NAME].max() <= 100.0, "Match score cannot exceed 100.0"

    def test_domain_weighting_monotonicity(self):
        """Tests that higher skill/portfolio produces higher match score under identical conditions."""
        low_skill_score = compute_match_score(
            skill_match=np.array([0.3]),
            portfolio_relevance=np.array([0.5]),
            experience=np.array([0.5]),
            budget_compatibility=np.array([0.5]),
            location_distance=np.array([0.5]),
            availability_time_compatibility=np.array([0.8]),
            noise_std=0.0,
        )[0]

        high_skill_score = compute_match_score(
            skill_match=np.array([0.9]),
            portfolio_relevance=np.array([0.5]),
            experience=np.array([0.5]),
            budget_compatibility=np.array([0.5]),
            location_distance=np.array([0.5]),
            availability_time_compatibility=np.array([0.8]),
            noise_std=0.0,
        )[0]

        assert high_skill_score > low_skill_score, "Higher skill match must increase score"


class TestModelArtifactAndMetadata:
    """Tests model training output, hyperparameter tuning, and evaluation metrics (ML-002, ML-007, ML-008, ML-009)."""

    def test_model_artifact_type(self):
        predictor = ModelPredictor.get_instance()
        assert predictor.is_loaded(), "Model artifact must be loaded"
        assert isinstance(predictor.model, DecisionTreeRegressor), (
            "Model MUST be an sklearn DecisionTreeRegressor (ML-002, docs/18-DECISIONS-AND-CONSTRAINTS.md)"
        )

    def test_model_metadata_metrics(self):
        predictor = ModelPredictor.get_instance()
        metadata = predictor.get_metadata()
        assert metadata, "Metadata must be present"

        # Check hyperparameters recorded
        hyperparams = metadata.get("hyperparameters", {})
        assert "min_samples_split" in hyperparams
        assert "min_samples_leaf" in hyperparams
        assert "max_depth" in hyperparams

        # Check metrics recorded (MAE, RMSE, R2)
        metrics = metadata.get("metrics", {})
        assert "train" in metrics
        assert "cross_validation_5fold" in metrics
        assert "test_holdout" in metrics

        test_metrics = metrics["test_holdout"]
        assert "mae" in test_metrics
        assert "rmse" in test_metrics
        assert "r2" in test_metrics

        # Performance thresholds: reasonable model quality without extreme overfitting
        assert test_metrics["r2"] > 0.70, f"Holdout R2 {test_metrics['r2']} should exceed 0.70"
        assert test_metrics["mae"] < 6.0, f"Holdout MAE {test_metrics['mae']} should be under 6.0"

    def test_feature_importances_exclude_ratings(self):
        predictor = ModelPredictor.get_instance()
        metadata = predictor.get_metadata()
        features_info = metadata.get("features", {})
        assert features_info.get("ratings_included") is False
        assert features_info.get("feature_count") == 6


class TestFastAPIPredictions:
    """Tests REST API endpoints (ML-010, ML-011)."""

    def test_health_check(self):
        response = client.get("/health")
        assert response.status_code == 200
        data = response.json()
        assert data["status"] == "UP"
        assert data["service"] == "studiolynk-ml-service"
        assert data["model_loaded"] is True

    def test_predict_match_snake_case(self):
        payload = {
            "skill_match": 0.85,
            "portfolio_relevance": 0.90,
            "experience": 0.70,
            "budget_compatibility": 0.80,
            "location_distance": 0.95,
            "availability_time_compatibility": 1.0,
        }
        response = client.post("/predict-match", json=payload)
        assert response.status_code == 200
        data = response.json()
        assert "matchScore" in data
        assert 0.0 <= data["matchScore"] <= 100.0
        assert data["status"] == "SUCCESS"

    def test_predict_match_camel_case(self):
        payload = {
            "skillMatch": 0.75,
            "portfolioRelevance": 0.80,
            "experience": 0.60,
            "budgetCompatibility": 0.85,
            "locationDistance": 0.90,
            "availabilityTimeCompatibility": 0.95,
        }
        response = client.post("/predict-match", json=payload)
        assert response.status_code == 200
        data = response.json()
        assert "matchScore" in data
        assert 0.0 <= data["matchScore"] <= 100.0

    def test_predict_match_validation_bounds_error(self):
        # Feature > 1.0 should trigger 422 Unprocessable Entity
        payload = {
            "skill_match": 1.5,
            "portfolio_relevance": 0.90,
            "experience": 0.70,
            "budget_compatibility": 0.80,
            "location_distance": 0.95,
            "availability_time_compatibility": 1.0,
        }
        response = client.post("/predict-match", json=payload)
        assert response.status_code == 422

        # Negative feature should also trigger 422
        payload["skill_match"] = -0.1
        response = client.post("/predict-match", json=payload)
        assert response.status_code == 422

    def test_predict_batch_endpoint(self):
        payload = {
            "candidates": [
                {
                    "candidateId": 1,
                    "features": {
                        "skill_match": 0.95,
                        "portfolio_relevance": 0.90,
                        "experience": 0.80,
                        "budget_compatibility": 0.90,
                        "location_distance": 0.95,
                        "availability_time_compatibility": 1.0,
                    },
                },
                {
                    "candidateId": 2,
                    "features": {
                        "skill_match": 0.30,
                        "portfolio_relevance": 0.25,
                        "experience": 0.20,
                        "budget_compatibility": 0.40,
                        "location_distance": 0.30,
                        "availability_time_compatibility": 0.50,
                    },
                },
            ]
        }
        response = client.post("/predict-batch", json=payload)
        assert response.status_code == 200
        data = response.json()
        assert data["total"] == 2
        assert len(data["predictions"]) == 2

        # Candidate 1 should score significantly higher than Candidate 2
        cand1 = next(c for c in data["predictions"] if c["candidateId"] == 1)
        cand2 = next(c for c in data["predictions"] if c["candidateId"] == 2)
        assert cand1["matchScore"] > cand2["matchScore"]
        assert 0.0 <= cand1["matchScore"] <= 100.0
        assert 0.0 <= cand2["matchScore"] <= 100.0

    def test_model_info_endpoint(self):
        response = client.get("/model-info")
        assert response.status_code == 200
        data = response.json()
        assert data["model_type"] == "sklearn.tree.DecisionTreeRegressor"
        assert "features" in data
        assert "metrics" in data
        assert "hyperparameters" in data
