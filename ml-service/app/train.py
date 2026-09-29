"""
Model Training & Hyperparameter Tuning Pipeline for StudioLynk Freelancer Matching.

Fulfills requirements:
- ML-002: Model shall be a Decision Tree Regressor (sklearn.tree.DecisionTreeRegressor).
- ML-004: Exactly 6 features in verified order.
- ML-005: Ratings strictly excluded.
- ML-007: Train/test split (80/20) and 5-fold cross-validation.
- ML-008: Evaluation with MAE, RMSE, and R2.
- ML-009: Tree hyperparameter tuning (max_depth, min_samples_split, min_samples_leaf) to prevent overfitting.
- Model artifact persistence: decision_tree_model.joblib and model_metadata.json.
"""

import json
import os
import sys
from datetime import datetime, timezone
from pathlib import Path
from typing import Dict, Any, Tuple

BASE_DIR = Path(__file__).resolve().parent.parent
if str(BASE_DIR) not in sys.path:
    sys.path.insert(0, str(BASE_DIR))

import joblib
import numpy as np
import pandas as pd
from sklearn.metrics import mean_absolute_error, mean_squared_error, r2_score
from sklearn.model_selection import GridSearchCV, KFold, cross_validate, train_test_split
from sklearn.tree import DecisionTreeRegressor

from app.dataset import (
    FEATURE_NAMES,
    TARGET_NAME,
    generate_synthetic_dataset,
    save_synthetic_dataset,
)

DATA_PATH = BASE_DIR / "data" / "synthetic_training_data.csv"
MODELS_DIR = BASE_DIR / "app" / "models"
MODEL_PATH = MODELS_DIR / "decision_tree_model.joblib"
METADATA_PATH = MODELS_DIR / "model_metadata.json"


def load_or_create_data() -> pd.DataFrame:
    """Loads existing synthetic dataset or generates it if missing."""
    if not DATA_PATH.exists():
        save_synthetic_dataset(str(DATA_PATH), n_samples=5000)
    return pd.read_csv(DATA_PATH)


def train_and_tune_model() -> Tuple[DecisionTreeRegressor, Dict[str, Any]]:
    """
    Executes complete ML training pipeline:
    1. Loads dataset of 5,000 synthetic records.
    2. Splits into 80% train and 20% test sets (random_state=42).
    3. Runs 5-fold GridSearchCV on DecisionTreeRegressor over tree hyperparameters.
    4. Computes train, 5-fold CV, and test evaluation metrics (MAE, RMSE, R2).
    5. Saves model artifact and comprehensive metadata.
    """
    df = load_or_create_data()

    X = df[FEATURE_NAMES]
    y = df[TARGET_NAME]

    # 80/20 Train/Test split
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.20, random_state=42, shuffle=True
    )

    # Hyperparameter search space
    param_grid = {
        "max_depth": [4, 6, 8, 10, 12, None],
        "min_samples_split": [2, 5, 10, 20],
        "min_samples_leaf": [1, 2, 5, 10],
    }

    base_estimator = DecisionTreeRegressor(random_state=42)

    # 5-fold cross validation
    cv = KFold(n_splits=5, shuffle=True, random_state=42)

    grid_search = GridSearchCV(
        estimator=base_estimator,
        param_grid=param_grid,
        scoring="neg_root_mean_squared_error",
        cv=cv,
        n_jobs=-1,
        return_train_score=True,
    )

    print("Running 5-fold cross-validation grid search on DecisionTreeRegressor...")
    grid_search.fit(X_train, y_train)

    best_model: DecisionTreeRegressor = grid_search.best_estimator_
    best_params = grid_search.best_params_
    print(f"Best hyperparameters found: {best_params}")

    # 5-fold CV evaluation with multiple metrics using best hyperparameters
    cv_results = cross_validate(
        best_model,
        X_train,
        y_train,
        cv=cv,
        scoring={
            "mae": "neg_mean_absolute_error",
            "rmse": "neg_root_mean_squared_error",
            "r2": "r2",
        },
        return_train_score=False,
    )

    cv_mae = float(-cv_results["test_mae"].mean())
    cv_rmse = float(-cv_results["test_rmse"].mean())
    cv_r2 = float(cv_results["test_r2"].mean())

    # Training set performance
    y_train_pred = best_model.predict(X_train)
    train_mae = float(mean_absolute_error(y_train, y_train_pred))
    train_rmse = float(np.sqrt(mean_squared_error(y_train, y_train_pred)))
    train_r2 = float(r2_score(y_train, y_train_pred))

    # Holdout test set performance
    y_test_pred = best_model.predict(X_test)
    test_mae = float(mean_absolute_error(y_test, y_test_pred))
    test_rmse = float(np.sqrt(mean_squared_error(y_test, y_test_pred)))
    test_r2 = float(r2_score(y_test, y_test_pred))

    # Feature importances
    feature_importances = {
        name: round(float(imp), 4)
        for name, imp in zip(FEATURE_NAMES, best_model.feature_importances_)
    }

    # Metadata dictionary
    metadata = {
        "model_type": "sklearn.tree.DecisionTreeRegressor",
        "model_version": "1.0.0",
        "trained_at": datetime.now(timezone.utc).isoformat(),
        "dataset": {
            "total_records": len(df),
            "train_records": len(X_train),
            "test_records": len(X_test),
            "train_split": 0.80,
            "test_split": 0.20,
            "cv_folds": 5,
            "random_state": 42,
            "source_file": "synthetic_training_data.csv",
        },
        "features": {
            "feature_names": FEATURE_NAMES,
            "feature_count": len(FEATURE_NAMES),
            "ratings_included": False,
            "feature_importances": feature_importances,
        },
        "hyperparameters": {
            "max_depth": best_params["max_depth"],
            "min_samples_split": best_params["min_samples_split"],
            "min_samples_leaf": best_params["min_samples_leaf"],
            "random_state": 42,
        },
        "metrics": {
            "train": {
                "mae": round(train_mae, 4),
                "rmse": round(train_rmse, 4),
                "r2": round(train_r2, 4),
            },
            "cross_validation_5fold": {
                "mae": round(cv_mae, 4),
                "rmse": round(cv_rmse, 4),
                "r2": round(cv_r2, 4),
            },
            "test_holdout": {
                "mae": round(test_mae, 4),
                "rmse": round(test_rmse, 4),
                "r2": round(test_r2, 4),
            },
        },
    }

    # Ensure models directory exists
    MODELS_DIR.mkdir(parents=True, exist_ok=True)

    # Save model artifact
    joblib.dump(best_model, MODEL_PATH)
    print(f"Model artifact saved to: {MODEL_PATH}")

    # Save metadata JSON
    with open(METADATA_PATH, "w", encoding="utf-8") as f:
        json.dump(metadata, f, indent=2)
    print(f"Model metadata saved to: {METADATA_PATH}")

    print("\n--- Model Training Summary ---")
    print(f"Hyperparameters: {best_params}")
    print(f"Train Metrics: MAE={train_mae:.3f}, RMSE={train_rmse:.3f}, R2={train_r2:.3f}")
    print(f"5-Fold CV Metrics: MAE={cv_mae:.3f}, RMSE={cv_rmse:.3f}, R2={cv_r2:.3f}")
    print(f"Test Metrics:  MAE={test_mae:.3f}, RMSE={test_rmse:.3f}, R2={test_r2:.3f}")
    print("Feature Importances:")
    for feat, imp in feature_importances.items():
        print(f"  - {feat}: {imp:.4f}")

    return best_model, metadata


if __name__ == "__main__":
    train_and_tune_model()
