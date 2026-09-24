# Implementation Plan

The agent must implement one phase at a time and stop for approval after each phase.

## Phase 0 — Architecture review
- Read all docs.
- Inspect environment/repository.
- Produce architecture proposal.
- Identify contradictions.
- Do not implement application features.
- Wait for approval.

## Phase 1 — EC2 and repository foundation
- Inspect Ubuntu 24.04.
- Install/configure required runtimes safely.
- Create repository structure.
- Configure Git.
- Create frontend, backend, ML service skeletons.
- Create `.env.example`.
- Establish health checks.
- Do not expose unnecessary ports publicly.

## Phase 2 — Spring Boot and MySQL foundation
- Configure MySQL.
- Create base entities.
- Flyway/Liquibase or another migration strategy if approved.
- Repository/service/controller structure.
- Global exception handling.
- Validation.
- Basic API documentation.

## Phase 3 — Authentication
- Registration.
- Login.
- JWT.
- Spring Security.
- Role handling.
- Password hashing.
- Forgot password OTP through SMTP.
- Password reset.
- Onboarding gate.

## Phase 4 — Studio onboarding/profile
- Studio onboarding.
- Address/location.
- Social links.
- Identity-document/declaration workflow.
- Profile editing.
- Profile completion.

## Phase 5 — Freelancer onboarding/profile
- Freelancer onboarding.
- Skills.
- Services.
- Equipment.
- Pricing.
- Profile editing.
- Profile completion.

## Phase 6 — S3 and portfolio
- S3 configuration.
- Profile image upload.
- Portfolio categories.
- Image CRUD.
- Drag/drop ordering.
- Portfolio UI.

## Phase 7 — Availability
- Rolling 10-day window.
- Available/Busy/Not Set.
- Time intervals.
- Availability APIs.
- Studio availability filtering.
- Conflict logic.

## Phase 8 — Freelancer discovery
- Normal search.
- Filters.
- Search result cards.
- Freelancer profile view.
- Location/distance.
- Full/half-day pricing display.

## Phase 9 — Work requirements
- Create/edit/view requirements.
- Draft/open/status lifecycle.
- Event-person private data.
- Requirement detail UI.

## Phase 10 — Work requests
- Send request.
- Receive request.
- Accept/reject.
- Multiple pending requests.
- Studio confirmation.
- Reveal private details only after confirmation.
- Final agreed price.
- Cancellation.
- Double booking protection.

## Phase 11 — Messaging
- WebSocket/STOMP.
- Requirement-specific conversations.
- Message persistence.
- Read/unread.
- Mobile/desktop chat UI.

## Phase 12 — Notifications
- Notification persistence.
- Event generation.
- Read/unread.
- Notification UI.

## Phase 13 — Ratings
- Two-way rating.
- Completed-work gate.
- Public profile display.
- Ensure ratings do not affect ML.

## Phase 14 — ML
- Generate 5,000 synthetic records.
- Document feature-generation rules.
- Train DecisionTreeRegressor.
- Cross-validation.
- Hyperparameter tuning.
- MAE/RMSE/R².
- Save model.
- Create FastAPI prediction endpoint.

## Phase 15 — ML integration
- Spring Boot -> FastAPI.
- Build feature extraction.
- Hard filtering first.
- ML ranking second.
- Match score UI.
- Integration tests.

## Phase 16 — Admin
- Admin login.
- Dashboard.
- Monitoring pages.
- No general edit/delete.
- Activity summaries.

## Phase 17 — UI/UX refinement
- Responsive desktop/mobile.
- Desktop top nav.
- Mobile bottom nav.
- Visual input cards.
- Icons.
- Loading/error/empty states.
- Profile polish.
- Accessibility.

## Phase 18 — Full testing and deployment
- Run unit/integration tests.
- End-to-end critical workflows.
- Build frontend.
- Build backend.
- Deploy services.
- Configure Nginx.
- Configure WebSocket proxy.
- Verify S3.
- Verify ML.
- Verify Elastic IP.
- Security review.
- Update README and deployment docs.

## Phase 19 — Academic finalization
- Architecture diagram.
- ER diagram.
- API documentation.
- ML methodology.
- Test case document.
- Demo data.
- Viva explanation notes.
- Final project status.
