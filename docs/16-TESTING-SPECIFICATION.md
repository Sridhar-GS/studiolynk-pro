# Testing Specification

## Frontend
- Component tests for important reusable components where practical.
- Form validation.
- Responsive/manual UI verification.
- Empty/loading/error states.

## Backend
Test:
- Registration
- Login
- JWT authorization
- Role authorization
- Password reset
- Onboarding
- Profile CRUD
- Portfolio CRUD
- Availability
- Search filters
- Requirement CRUD
- Request workflow
- Confirmation
- Cancellation
- Double booking
- Ratings
- Notifications

## Availability cases
1. Available date/time -> visible.
2. Busy -> hidden.
3. Not Set -> hidden.
4. Requested time outside availability -> hidden.
5. Date beyond 10-day window -> candidate may appear as availability unknown.
6. Existing confirmed booking -> conflicting confirmation rejected.

## Request cases
1. Studio sends request.
2. Freelancer sees limited information.
3. Freelancer messages.
4. Freelancer accepts.
5. Studio confirms.
6. Private event details become visible only after confirmation.
7. Other pending requests close.
8. Cancellation requires reason.

## ML
Test:
- Dataset generation.
- Feature ranges.
- Train/test split.
- Model training.
- Prediction output range.
- API input validation.
- API output.
- Evaluation metrics.
- No NaN/invalid values.

## Integration
Test:
React -> Spring Boot -> MySQL.
Spring Boot -> S3.
Spring Boot -> FastAPI.
WebSocket messaging.

## Deployment
Verify:
- React loads through Elastic IP.
- API works through Nginx.
- WebSocket works through Nginx.
- S3 uploads work.
- MySQL is private.
- ML service works.
