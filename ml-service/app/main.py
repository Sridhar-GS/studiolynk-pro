"""
StudioLynk ML Microservice
FastAPI service exposing Decision Tree Regressor match predictions and health status.

Endpoints:
- POST /predict-match: Predict 0-100 match score for a single candidate
- POST /predict-batch: Predict scores for multiple candidates for efficient ranking
- GET /model-info: Inspect model type, hyperparameters, evaluation metrics, and feature importances
- GET /health: Operational health and model loading status
"""

import sys
from pathlib import Path
from typing import Dict, Any
from contextlib import asynccontextmanager

from fastapi import FastAPI, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field

BASE_DIR = Path(__file__).resolve().parent.parent
if str(BASE_DIR) not in sys.path:
    sys.path.insert(0, str(BASE_DIR))

from app.schemas import (
    MatchPredictionRequest,
    MatchPredictionResponse,
    BatchMatchPredictionRequest,
    BatchMatchPredictionResponse,
    ModelInfoResponse,
)
from app.predictor import ModelPredictor


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Eagerly initialize and warm up model predictor on startup
    predictor = ModelPredictor.get_instance()
    if not predictor.is_loaded():
        predictor.load_model()
    yield


app = FastAPI(
    title="StudioLynk ML Service",
    description="Microservice providing AI/ML freelancer matching for StudioLynk using Decision Tree Regressor",
    version="1.0.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


class HealthResponse(BaseModel):
    model_config = {"protected_namespaces": ()}

    status: str = Field(..., examples=["UP"])
    service: str = Field(..., examples=["studiolynk-ml-service"])
    version: str = Field(..., examples=["1.0.0"])
    model_loaded: bool = Field(..., examples=[True])


@app.get("/health", response_model=HealthResponse, tags=["Health"])
def health_check() -> Dict[str, Any]:
    """
    Health check endpoint returning microservice and model loading status.
    """
    predictor = ModelPredictor.get_instance()
    return {
        "status": "UP",
        "service": "studiolynk-ml-service",
        "version": "1.0.0",
        "model_loaded": predictor.is_loaded(),
    }


@app.get("/", tags=["Root"])
def root_info() -> Dict[str, Any]:
    return {
        "message": "StudioLynk ML Service is operational",
        "docs": "/docs",
        "health": "/health",
        "model_info": "/model-info",
        "predict_match": "/predict-match",
        "predict_batch": "/predict-batch",
    }


@app.post(
    "/predict-match",
    response_model=MatchPredictionResponse,
    status_code=status.HTTP_200_OK,
    tags=["Prediction"],
    summary="Predict match score for a single candidate",
)
def predict_match(request: MatchPredictionRequest) -> MatchPredictionResponse:
    """
    Predicts a 0-100 match score for a single freelancer candidate.
    Expects 6 normalized features (range 0.0 - 1.0).
    Ratings are strictly excluded from prediction.
    """
    predictor = ModelPredictor.get_instance()
    if not predictor.is_loaded():
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="ML Model is not currently loaded or available.",
        )

    score = predictor.predict_single(request)
    return MatchPredictionResponse(matchScore=score, status="SUCCESS")


@app.post(
    "/predict-batch",
    response_model=BatchMatchPredictionResponse,
    status_code=status.HTTP_200_OK,
    tags=["Prediction"],
    summary="Batch predict match scores for multiple candidates",
)
def predict_batch(request: BatchMatchPredictionRequest) -> BatchMatchPredictionResponse:
    """
    Predicts 0-100 match scores for a batch of candidate freelancers.
    Enables Spring Boot to rank multiple pre-filtered candidates efficiently.
    """
    predictor = ModelPredictor.get_instance()
    if not predictor.is_loaded():
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="ML Model is not currently loaded or available.",
        )

    predictions = predictor.predict_batch(request.candidates)
    return BatchMatchPredictionResponse(
        predictions=predictions,
        total=len(predictions),
        status="SUCCESS",
    )


@app.get(
    "/model-info",
    response_model=ModelInfoResponse,
    tags=["Metadata"],
    summary="Get model architecture, hyperparameters, and evaluation metrics",
)
def get_model_info() -> ModelInfoResponse:
    """
    Returns complete metadata regarding the trained DecisionTreeRegressor:
    hyperparameters, dataset properties, feature importances, and MAE/RMSE/R2 metrics.
    """
    predictor = ModelPredictor.get_instance()
    metadata = predictor.get_metadata()
    if not metadata:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Model metadata is not available.",
        )
    return ModelInfoResponse(**metadata)


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True)
