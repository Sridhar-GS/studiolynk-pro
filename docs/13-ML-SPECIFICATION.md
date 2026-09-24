# ML Specification

## Objective
Predict a freelancer suitability/match score from 0 to 100 for a Studio work requirement.

## Model
`sklearn.tree.DecisionTreeRegressor`

Do not substitute a Random Forest or LLM unless explicitly approved.

## Features
1. skill_match
2. portfolio_relevance
3. experience
4. budget_compatibility
5. location_distance
6. availability_time_compatibility

Ratings are excluded.

## Feature design
Features must be deterministic and documented.

Examples:
- Skill match: proportion/weighted overlap between required and freelancer skills.
- Portfolio relevance: overlap between requirement services/categories and freelancer portfolio categories plus approved metadata/text.
- Experience: normalized experience value.
- Budget compatibility: relationship between studio budget and freelancer full/half-day charge.
- Location distance: geographic distance derived from coordinates where available.
- Availability/time compatibility: binary or normalized compatibility after hard availability filtering.

## Dataset
Approximately 5,000 synthetic records.

Dataset generation must use documented business rules rather than arbitrary random target values.

The dataset should include enough variation for the model to learn meaningful relationships.

## Split
- 80% training
- 20% testing

Within training:
- 5-fold cross-validation.

## Hyperparameter tuning
Evaluate sensible ranges for:
- max_depth
- min_samples_split
- min_samples_leaf

Do not over-tune on the test set.

## Metrics
- MAE
- RMSE
- R²

Compare training and validation/test performance to identify overfitting/underfitting.

## Model artifact
Save the approved model artifact in a controlled ML directory.
Record:
- feature order
- preprocessing
- model parameters
- training dataset version
- evaluation metrics

## FastAPI
Endpoint:
`POST /predict-match`

Input contains all required feature values.

Output:
```json
{
  "matchScore": 92.4
}
```

## Ranking
Spring Boot:
1. Applies hard filters.
2. Sends eligible candidate features to ML.
3. Receives scores.
4. Sorts descending.
5. Returns ranked freelancer cards.

## Important
AI ranking is assistance, not an automated hiring decision. Studio makes the final selection.
