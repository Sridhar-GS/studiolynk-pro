"""
StudioLynk ML Microservice
FastAPI service exposing Decision Tree Regressor match predictions and health status.
"""
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from typing import Dict, Any

app = FastAPI(
    title="StudioLynk ML Service",
    description="Microservice providing AI/ML freelancer matching for StudioLynk",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

class HealthResponse(BaseModel):
    status: str = Field(..., example="UP")
    service: str = Field(..., example="studiolynk-ml-service")
    version: str = Field(..., example="1.0.0")

@app.get("/health", response_model=HealthResponse, tags=["Health"])
def health_check() -> Dict[str, Any]:
    """
    Health check endpoint for StudioLynk ML Service.
    """
    return {
        "status": "UP",
        "service": "studiolynk-ml-service",
        "version": "1.0.0"
    }

@app.get("/", tags=["Root"])
def root_info() -> Dict[str, Any]:
    return {
        "message": "StudioLynk ML Service is operational",
        "docs": "/docs",
        "health": "/health"
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True)
