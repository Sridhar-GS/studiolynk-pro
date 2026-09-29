"""
Inference Engine and Model Manager for StudioLynk Freelancer Matching.
Loads the trained DecisionTreeRegressor and provides single and batch prediction methods.
"""

import json
from pathlib import Path
from typing import Dict, Any, List, Optional
import joblib
import numpy as np
import pandas as pd

from app.dataset import FEATURE_NAMES
from app.schemas import (
    MatchFeatures,
    CandidateFeatureItem,
    CandidateScoreItem,
)

BASE_DIR = Path(__file__).resolve().parent.parent
MODEL_PATH = BASE_DIR / "app" / "models" / "decision_tree_model.joblib"
METADATA_PATH = BASE_DIR / "app" / "models" / "model_metadata.json"


class ModelPredictor:
    """
    Singleton predictor encapsulating the trained DecisionTreeRegressor.
    """
    _instance: Optional["ModelPredictor"] = None

    def __init__(self):
        self.model = None
        self.metadata: Dict[str, Any] = {}
        self.load_model()

    @classmethod
    def get_instance(cls) -> "ModelPredictor":
        if cls._instance is None:
            cls._instance = cls()
        return cls._instance

    def load_model(self) -> None:
        """
        Loads model artifact and metadata from disk.
        If artifacts are missing, triggers training pipeline.
        """
        if not MODEL_PATH.exists() or not METADATA_PATH.exists():
            from app.train import train_and_tune_model
            print("Model artifacts not found. Training model now...")
            self.model, self.metadata = train_and_tune_model()
            return

        try:
            self.model = joblib.load(MODEL_PATH)
            with open(METADATA_PATH, "r", encoding="utf-8") as f:
                self.metadata = json.load(f)
            print(f"Loaded DecisionTreeRegressor model from {MODEL_PATH}")
        except Exception as e:
            print(f"Error loading model artifact: {e}")
            self.model = None
            self.metadata = {}

    def is_loaded(self) -> bool:
        return self.model is not None

    def predict_single(self, features: MatchFeatures) -> float:
        """
        Predicts match score for a single freelancer candidate.
        Returns score strictly bounded between 0.0 and 100.0, rounded to 1 decimal place.
        """
        if self.model is None:
            raise RuntimeError("ML model is not loaded.")

        df = pd.DataFrame([features.to_feature_vector()], columns=FEATURE_NAMES)
        raw_score = float(self.model.predict(df)[0])
        bounded_score = float(np.clip(raw_score, 0.0, 100.0))
        return round(bounded_score, 1)

    def predict_batch(self, candidates: List[CandidateFeatureItem]) -> List[CandidateScoreItem]:
        """
        Predicts match scores for a batch of candidates in a vectorized operation.
        Returns list of CandidateScoreItem with candidateId and matchScore.
        """
        if not candidates:
            return []

        if self.model is None:
            raise RuntimeError("ML model is not loaded.")

        rows = [c.features.to_feature_vector() for c in candidates]
        df = pd.DataFrame(rows, columns=FEATURE_NAMES)
        raw_scores = self.model.predict(df)
        bounded_scores = np.clip(raw_scores, 0.0, 100.0)

        results: List[CandidateScoreItem] = []
        for candidate, score in zip(candidates, bounded_scores):
            results.append(
                CandidateScoreItem(
                    candidate_id=candidate.candidate_id,
                    match_score=round(float(score), 1)
                )
            )
        return results

    def get_metadata(self) -> Dict[str, Any]:
        """Returns loaded model metadata."""
        return self.metadata
