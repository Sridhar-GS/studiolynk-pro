# Backend API Specification

Base path: `/api`

## Auth
- POST `/auth/register`
- POST `/auth/login`
- POST `/auth/forgot-password`
- POST `/auth/verify-reset-otp`
- POST `/auth/reset-password`
- GET `/auth/me`

## Onboarding
- GET `/onboarding/status`
- POST `/onboarding/studio`
- POST `/onboarding/freelancer`
- PUT `/onboarding/studio`
- PUT `/onboarding/freelancer`

## Studio
- GET `/studios/me`
- PUT `/studios/me`
- GET `/studios/{id}`

## Freelancer
- GET `/freelancers/me`
- PUT `/freelancers/me`
- GET `/freelancers/{id}`
- GET `/freelancers/search`

## Skills/services/equipment
- GET `/skills`
- POST `/skills/custom`
- GET `/services`
- POST `/services/custom`
- GET `/equipment/categories`
- GET `/equipment`
- POST `/equipment/custom`

## Portfolio
- GET `/freelancers/me/portfolio`
- POST `/freelancers/me/portfolio/categories`
- PUT `/freelancers/me/portfolio/categories/{id}`
- DELETE `/freelancers/me/portfolio/categories/{id}`
- POST `/freelancers/me/portfolio/images`
- PUT `/freelancers/me/portfolio/images/{id}`
- DELETE `/freelancers/me/portfolio/images/{id}`
- PUT `/freelancers/me/portfolio/reorder`

## Availability
- GET `/freelancers/me/availability`
- PUT `/freelancers/me/availability`
- POST `/freelancers/me/availability/reset`
- GET `/freelancers/{id}/availability`

## Work requirements
- POST `/requirements`
- GET `/requirements`
- GET `/requirements/{id}`
- PUT `/requirements/{id}`
- DELETE `/requirements/{id}` for drafts only
- POST `/requirements/{id}/open`
- POST `/requirements/{id}/cancel`
- POST `/requirements/{id}/complete`
- POST `/requirements/{id}/start`

## Requests
- POST `/requirements/{id}/requests`
- GET `/requests/received`
- GET `/requests/sent`
- GET `/requests/{id}`
- POST `/requests/{id}/accept`
- POST `/requests/{id}/reject`
- POST `/requirements/{id}/confirm/{requestId}`

## Ratings
- POST `/requirements/{id}/ratings`
- GET `/freelancers/{id}/ratings`
- GET `/studios/{id}/ratings`

## Notifications
- GET `/notifications`
- POST `/notifications/{id}/read`
- POST `/notifications/read-all`

## Admin
- GET `/admin/dashboard`
- GET `/admin/studios`
- GET `/admin/freelancers`
- GET `/admin/requirements`
- GET `/admin/requests`
- GET `/admin/activity`

Exact DTOs, validation constraints, authorization rules, pagination, sorting, and error formats must be documented before implementation of each relevant module.
