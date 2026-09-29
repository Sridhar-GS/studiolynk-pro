"""
Synthetic dataset generator for StudioLynk Freelancer Matching Model.
Generates 5,000 synthetic records using documented business domain logic.

Requirements fulfilled:
- ML-003: ~5,000 logically generated synthetic records
- ML-004: Exactly 6 features (skill_match, portfolio_relevance, experience,
          budget_compatibility, location_distance, availability_time_compatibility)
- ML-005: Ratings are strictly EXCLUDED as an ML feature
- ML-006: Explicit logical rules and domain weighting, not arbitrary random labels
"""

import numpy as np
import pandas as pd
from typing import Tuple, Optional
from pathlib import Path

FEATURE_NAMES = [
    "skill_match",
    "portfolio_relevance",
    "experience",
    "budget_compatibility",
    "location_distance",
    "availability_time_compatibility",
]

TARGET_NAME = "match_score"


def compute_match_score(
    skill_match: np.ndarray,
    portfolio_relevance: np.ndarray,
    experience: np.ndarray,
    budget_compatibility: np.ndarray,
    location_distance: np.ndarray,
    availability_time_compatibility: np.ndarray,
    noise_std: float = 1.5,
    random_state: Optional[int] = 42,
) -> np.ndarray:
    """
    Computes deterministic match scores (0-100) using domain rules and realistic business interactions:
    - Base weights:
        * skill_match: 30% (primary technical prerequisite)
        * portfolio_relevance: 25% (visual style / category alignment)
        * budget_compatibility: 15% (commercial viability)
        * availability_time_compatibility: 15% (schedule feasibility)
        * location_distance: 10% (geographic convenience)
        * experience: 5% (craft maturity)
    - Domain interaction rules:
        * Severe skill penalty: If skill_match < 0.25, shoot cannot be fulfilled effectively.
        * Availability penalty: If availability < 0.15, severe schedule conflict.
        * Synergy bonus: If both skill >= 0.85 and portfolio >= 0.85, +3.0 boost.
        * Budget friction: If budget < 0.20, -10% score reduction due to commercial mismatch.
    - Controlled Gaussian noise to simulate minor unmeasured studio preferences.
    - Strict clipping to [0.0, 100.0].
    """
    if random_state is not None:
        rng = np.random.RandomState(random_state)
    else:
        rng = np.random.RandomState()

    # 1. Linear weighted base score (0 - 100)
    base_score = 100.0 * (
        0.30 * skill_match
        + 0.25 * portfolio_relevance
        + 0.15 * budget_compatibility
        + 0.15 * availability_time_compatibility
        + 0.10 * location_distance
        + 0.05 * experience
    )

    # 2. Non-linear business rules and penalties
    # Severe skill penalty if technical capability is lacking
    low_skill_mask = skill_match < 0.25
    skill_penalty_factor = np.where(low_skill_mask, 0.40 + 0.60 * (skill_match / 0.25), 1.0)
    score = base_score * skill_penalty_factor

    # Severe availability penalty if freelancer has timing clash
    low_avail_mask = availability_time_compatibility < 0.15
    avail_penalty_factor = np.where(low_avail_mask, 0.50 + 0.50 * (availability_time_compatibility / 0.15), 1.0)
    score = score * avail_penalty_factor

    # Budget friction penalty if rates are excessively incompatible
    low_budget_mask = budget_compatibility < 0.20
    budget_penalty_factor = np.where(low_budget_mask, 0.90, 1.0)
    score = score * budget_penalty_factor

    # Synergy bonus if both skill and portfolio relevance are exceptional
    synergy_mask = (skill_match >= 0.85) & (portfolio_relevance >= 0.85)
    score = np.where(synergy_mask, score + 3.0, score)

    # 3. Add controlled realistic noise
    if noise_std > 0:
        noise = rng.normal(loc=0.0, scale=noise_std, size=score.shape)
        score = score + noise

    # 4. Strict bounding to [0.0, 100.0] and rounding
    score = np.clip(score, 0.0, 100.0)
    return np.round(score, 1)


def generate_synthetic_dataset(
    n_samples: int = 5000,
    random_state: int = 42,
    noise_std: float = 1.5,
) -> pd.DataFrame:
    """
    Generates n_samples of synthetic freelancer matching records across all 6 features.
    Features follow realistic distributions (Beta / Uniform) reflecting platform creator distributions.
    """
    rng = np.random.RandomState(random_state)

    # Generate feature distributions:
    # skill_match: Beta(3, 2) shifted slightly towards higher competence but covering full [0, 1]
    skill_match = rng.beta(a=3.0, b=2.0, size=n_samples)
    skill_match = np.clip(skill_match, 0.0, 1.0)

    # portfolio_relevance: Beta(2.5, 2.0)
    portfolio_relevance = rng.beta(a=2.5, b=2.0, size=n_samples)
    portfolio_relevance = np.clip(portfolio_relevance, 0.0, 1.0)

    # experience: Beta(2.0, 3.0) - more early-to-mid career creators, fewer veterans
    experience = rng.beta(a=2.0, b=3.0, size=n_samples)
    experience = np.clip(experience, 0.0, 1.0)

    # budget_compatibility: Beta(3.0, 2.5) - healthy concentration around budget with variations
    budget_compatibility = rng.beta(a=3.0, b=2.5, size=n_samples)
    budget_compatibility = np.clip(budget_compatibility, 0.0, 1.0)

    # location_distance: Uniform(0.1, 1.0) with some near-zero distance entries
    location_distance = rng.uniform(low=0.0, high=1.0, size=n_samples)

    # availability_time_compatibility: Mostly high compatibility since hard filtering precedes ML
    # Beta(4.0, 1.5) reflects candidates who have passed availability pre-check
    availability_time_compatibility = rng.beta(a=4.0, b=1.5, size=n_samples)
    availability_time_compatibility = np.clip(availability_time_compatibility, 0.0, 1.0)

    # Round feature inputs to 4 decimal places for clean storage
    skill_match = np.round(skill_match, 4)
    portfolio_relevance = np.round(portfolio_relevance, 4)
    experience = np.round(experience, 4)
    budget_compatibility = np.round(budget_compatibility, 4)
    location_distance = np.round(location_distance, 4)
    availability_time_compatibility = np.round(availability_time_compatibility, 4)

    # Compute target match score
    match_score = compute_match_score(
        skill_match=skill_match,
        portfolio_relevance=portfolio_relevance,
        experience=experience,
        budget_compatibility=budget_compatibility,
        location_distance=location_distance,
        availability_time_compatibility=availability_time_compatibility,
        noise_std=noise_std,
        random_state=random_state + 100,
    )

    df = pd.DataFrame({
        "skill_match": skill_match,
        "portfolio_relevance": portfolio_relevance,
        "experience": experience,
        "budget_compatibility": budget_compatibility,
        "location_distance": location_distance,
        "availability_time_compatibility": availability_time_compatibility,
        "match_score": match_score,
    })

    return df


def save_synthetic_dataset(output_path: Optional[str] = None, n_samples: int = 5000) -> str:
    """
    Generates and saves the synthetic dataset to CSV.
    """
    if output_path is None:
        base_dir = Path(__file__).resolve().parent.parent
        output_path = str(base_dir / "data" / "synthetic_training_data.csv")

    Path(output_path).parent.mkdir(parents=True, exist_ok=True)
    df = generate_synthetic_dataset(n_samples=n_samples, random_state=42)
    df.to_csv(output_path, index=False)
    return output_path


if __name__ == "__main__":
    saved_file = save_synthetic_dataset()
    print(f"Generated synthetic dataset saved to: {saved_file}")
    df_sample = pd.read_csv(saved_file)
    print(f"Dataset shape: {df_sample.shape}")
    print("Summary statistics:")
    print(df_sample.describe().round(3))
