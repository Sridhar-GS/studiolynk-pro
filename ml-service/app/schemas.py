"""
Pydantic Schemas for StudioLynk ML Microservice.
Defines strict validation for single and batch match score prediction endpoints.
Supports both camelCase and snake_case inputs for seamless Spring Boot integration.
"""

from typing import List, Optional, Dict, Any
from pydantic import BaseModel, Field, ConfigDict


class MatchFeatures(BaseModel):
    """
    Features required for DecisionTreeRegressor match prediction.
    All features must be normalized between 0.0 and 1.0.
    Ratings are strictly EXCLUDED (ML-005).
    """
    model_config = ConfigDict(populate_by_name=True)

    skill_match: float = Field(
        ...,
        ge=0.0,
        le=1.0,
        alias="skillMatch",
        description="Skill overlap between studio requirement and freelancer skills (0.0 to 1.0)",
        examples=[0.85],
    )
    portfolio_relevance: float = Field(
        ...,
        ge=0.0,
        le=1.0,
        alias="portfolioRelevance",
        description="Category and style alignment between requirement and portfolio (0.0 to 1.0)",
        examples=[0.90],
    )
    experience: float = Field(
        ...,
        ge=0.0,
        le=1.0,
        description="Normalized years of experience (0.0 to 1.0)",
        examples=[0.70],
    )
    budget_compatibility: float = Field(
        ...,
        ge=0.0,
        le=1.0,
        alias="budgetCompatibility",
        description="Ratio of studio budget vs freelancer rate (0.0 to 1.0)",
        examples=[0.80],
    )
    location_distance: float = Field(
        ...,
        ge=0.0,
        le=1.0,
        alias="locationDistance",
        description="Geographic proximity score (1.0 = adjacent, 0.0 = max radius)",
        examples=[0.95],
    )
    availability_time_compatibility: float = Field(
        ...,
        ge=0.0,
        le=1.0,
        alias="availabilityTimeCompatibility",
        description="Schedule compatibility score after hard availability filtering (0.0 to 1.0)",
        examples=[1.0],
    )

    def to_feature_vector(self) -> List[float]:
        """
        Returns feature vector strictly matching trained model feature order:
        [skill_match, portfolio_relevance, experience, budget_compatibility, location_distance, availability_time_compatibility]
        """
        return [
            float(self.skill_match),
            float(self.portfolio_relevance),
            float(self.experience),
            float(self.budget_compatibility),
            float(self.location_distance),
            float(self.availability_time_compatibility),
        ]


class MatchPredictionRequest(MatchFeatures):
    """
    Single freelancer prediction request body.
    Inherits all 6 normalized features directly.
    """
    pass


class MatchPredictionResponse(BaseModel):
    """
    Prediction response returning 0-100 match score (ML-011).
    """
    matchScore: float = Field(
        ...,
        ge=0.0,
        le=100.0,
        description="Predicted match suitability score from 0.0 to 100.0",
        examples=[92.4],
    )
    status: str = Field(default="SUCCESS", examples=["SUCCESS"])


class CandidateFeatureItem(BaseModel):
    """
    Individual candidate payload inside a batch request.
    """
    model_config = ConfigDict(populate_by_name=True)

    candidate_id: Any = Field(
        ...,
        alias="candidateId",
        description="Unique identifier of freelancer/candidate",
        examples=[101],
    )
    features: MatchFeatures = Field(
        ...,
        description="Normalized 6-feature payload for candidate",
    )


class BatchMatchPredictionRequest(BaseModel):
    """
    Batch prediction request for ranking multiple candidates in a single call.
    """
    candidates: List[CandidateFeatureItem] = Field(
        ...,
        description="List of candidates and their corresponding features",
    )


class CandidateScoreItem(BaseModel):
    """
    Individual scored candidate in batch response.
    """
    model_config = ConfigDict(populate_by_name=True)

    candidate_id: Any = Field(
        ...,
        alias="candidateId",
        description="Candidate identifier",
        examples=[101],
    )
    match_score: float = Field(
        ...,
        ge=0.0,
        le=100.0,
        alias="matchScore",
        description="Predicted match score from 0.0 to 100.0",
        examples=[88.5],
    )


class BatchMatchPredictionResponse(BaseModel):
    """
    Batch prediction response with ranked scores.
    """
    model_config = ConfigDict(populate_by_name=True)

    predictions: List[CandidateScoreItem] = Field(
        ...,
        description="Scored candidate results",
    )
    total: int = Field(..., description="Total candidates processed")
    status: str = Field(default="SUCCESS")


class ModelInfoResponse(BaseModel):
    """
    Metadata about the deployed DecisionTreeRegressor model.
    """
    model_config = ConfigDict(protected_namespaces=())

    model_type: str
    model_version: str
    trained_at: Optional[str] = None
    dataset: Dict[str, Any]
    features: Dict[str, Any]
    hyperparameters: Dict[str, Any]
    metrics: Dict[str, Any]
