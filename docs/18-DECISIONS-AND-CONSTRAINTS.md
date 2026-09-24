# Decisions and Constraints

## Product
- Project name: StudioLynk.
- Final-year academic prototype.
- Target users: Studios and Freelancers.
- No Client role.

## Authentication
- Email/password.
- JWT.
- Spring Security.
- Email OTP password reset.
- No Firebase.

## Studio
- Studio may enter platform after onboarding.
- No admin approval.
- Prototype identity-document/declaration only.
- No external Aadhaar verification.
- No Aadhaar number storage.

## Freelancer
- No admin verification.
- Professional profile, portfolio, equipment, pricing, availability.

## Portfolio
- One portfolio per freelancer.
- Category-based.
- Multiple images.
- Full CRUD.
- Drag/drop reorder.
- No public/private toggle.
- S3 storage.

## Availability
- Rolling 10 days.
- Available/Busy/Not Set.
- Time ranges.
- Hard filter before ML.
- Beyond 10 days: unknown availability.

## Work
- Multiple freelancers may be requested.
- One final freelancer confirmed in the prototype.
- Messaging before acceptance.
- Private event-person details after Studio confirmation.
- Both parties can cancel with reason.
- No payments.

## Ratings
- Two-way.
- Public.
- Post-completion.
- Not used by ML.

## AI
- Decision Tree Regressor.
- 5,000 synthetic records.
- Score 0–100.
- Features: skill, portfolio, experience, budget, location, availability/time.
- Ratings excluded.
- FastAPI.

## Infrastructure
- Ubuntu 24.04 EC2.
- MySQL on EC2.
- S3 for images.
- Elastic IP.
- Nginx for final demo.
- VS Code Remote SSH.

## Explicit non-goals
- No Firebase.
- No MongoDB.
- No RDS.
- No payment gateway.
- No client accounts.
- No LLM recommendation.
- No computer vision.
- No Kubernetes.
- No Kafka.
- No Redis unless later approved.
- No unnecessary microservices.
