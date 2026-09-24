# StudioLynk Requirements

Status values: `PLANNED`, `IN PROGRESS`, `IMPLEMENTED`, `TESTED`.

## Authentication
- AUTH-001 — User registration shall use email and password.
- AUTH-002 — A single login page shall route users according to account role.
- AUTH-003 — Password reset shall use an email-delivered OTP.
- AUTH-004 — Firebase shall not be used for authentication or password reset.
- AUTH-005 — Password reset OTP shall expire after 5 minutes.
- AUTH-006 — OTP resend shall use a cooldown and attempt limits.
- AUTH-007 — Password policy shall enforce at least 8 characters, uppercase, lowercase, number, and special character.
- AUTH-008 — JWT authentication and Spring Security shall protect authenticated APIs.

## Account onboarding
- ONB-001 — Signup shall lead to role selection before role-specific onboarding.
- ONB-002 — Only onboarding-completed accounts may enter the main platform.
- ONB-003 — After onboarding completion, the user shall be redirected to the appropriate profile page.
- ONB-004 — Users may leave incomplete onboarding and resume it after login.
- ONB-005 — Profile completion percentage may be shown to encourage complete profiles.

## Studio
- STU-001 — Studio onboarding shall collect studio name, studio photo/logo, owner name, email, phone, address, map location, years of operation, and social/website links.
- STU-002 — Studio shall submit an owner identity document/declaration as a prototype identity workflow.
- STU-003 — No external Aadhaar verification API shall be implemented.
- STU-004 — Studio may use the platform after completing onboarding; no admin approval is required.
- STU-005 — Studio shall be able to edit its profile.
- STU-006 — Studio shall be able to create work/event requirements.

## Freelancer
- FRL-001 — Freelancer onboarding shall collect name, photo, email, phone, address, location, experience, bio, skills, services, equipment, and full/half-day charges.
- FRL-002 — Skills shall support predefined options and custom additions.
- FRL-003 — Services shall support predefined options and custom additions.
- FRL-004 — Equipment shall be grouped by categories such as Camera, Lens, Lighting, Software, and Other.
- FRL-005 — Equipment shall support predefined options and custom additions.
- FRL-006 — Freelancer shall be able to edit professional profile data.
- FRL-007 — Freelancer shall not require admin verification.

## Portfolio
- POR-001 — Each freelancer shall maintain one portfolio.
- POR-002 — Portfolio shall support categories.
- POR-003 — Portfolio categories may be selected/created by the freelancer.
- POR-004 — A freelancer shall upload multiple images per category.
- POR-005 — Portfolio supports create/read/update/delete.
- POR-006 — Portfolio images shall support drag-and-drop reordering.
- POR-007 — Portfolio has no public/private toggle.
- POR-008 — Portfolio images shall be stored in AWS S3.
- POR-009 — Portfolio relevance for ML shall use selected category plus portfolio metadata/text, not computer vision.

## Availability
- AVL-001 — Freelancer availability shall use a rolling 10-day window.
- AVL-002 — Each day shall be Available, Busy, or Not Set.
- AVL-003 — Available days shall have start and end time.
- AVL-004 — Studio search within the 10-day window shall show only Available freelancers matching the requested time.
- AVL-005 — Busy and Not Set days shall not appear as available.
- AVL-006 — Dates beyond the 10-day window may list matching freelancers but shall clearly indicate availability is not set/unknown.
- AVL-007 — Availability shall be used as a hard filter before AI ranking.
- AVL-008 — Confirmed work shall prevent conflicting bookings.
- AVL-009 — Confirmed work shall make the relevant freelancer time unavailable/busy.

## Studio discovery
- DIS-001 — Studio shall have normal freelancer search.
- DIS-002 — Studio shall have AI requirement-based matching.
- DIS-003 — Search filters shall include service, skill, location, date, time, full/half day, budget, experience, and equipment.
- DIS-004 — Search results shall be displayed as individual freelancer cards.
- DIS-005 — Freelancer cards shall show profile, location, rating, experience, skills/services, relevant availability, and pricing.
- DIS-006 — AI results shall display a match score such as `92.4% Match`.

## Work requirements
- WRK-001 — Studio shall create detailed work/event requirements.
- WRK-002 — Requirement shall include event name/type, date, start/end time, location, required services, required skills, equipment needs, budget, description, and private event-person details where applicable.
- WRK-003 — Requirement shall support statuses such as Draft, Open, Requested, Accepted, Confirmed, In Progress, Completed, and Cancelled.
- WRK-004 — Studio may search without creating a requirement.
- WRK-005 — Studio may use a requirement to obtain AI-ranked freelancers.
- WRK-006 — Multiple freelancers may receive requests for one requirement.
- WRK-007 — Studio ultimately confirms one freelancer for the prototype workflow.
- WRK-008 — When one freelancer is confirmed, other pending requests for that requirement shall be closed/rejected.

## Work requests and privacy
- REQ-001 — Studio can send a work request to a freelancer.
- REQ-002 — Freelancer initially sees event/work details but not private event-person contact details.
- REQ-003 — Freelancer can message the studio before acceptance.
- REQ-004 — Freelancer can accept or reject a request.
- REQ-005 — Studio confirms the final freelancer.
- REQ-006 — Private event-person contact information is revealed only after Studio confirmation.
- REQ-007 — Both parties may cancel confirmed work with a reason.
- REQ-008 — The final agreed price shall be stored on the work record after negotiation/confirmation.
- REQ-009 — Freelancer double booking shall be prevented.

## Messaging
- MSG-001 — Messaging shall be requirement-specific.
- MSG-002 — Messaging shall use WebSocket/STOMP.
- MSG-003 — Messages shall be text-only.
- MSG-004 — Messages shall have timestamps and read/unread state.
- MSG-005 — Studio and freelancer may negotiate through messages.
- MSG-006 — No external email notifications are required for messaging.

## Notifications
- NOT-001 — Notifications shall be in-app.
- NOT-002 — Notifications shall include new requests, acceptance/rejection, confirmation, messages, work lifecycle events, and rating reminders.
- NOT-003 — Notifications shall have read/unread state.
- NOT-004 — Users shall be able to mark all notifications as read.

## Ratings
- RAT-001 — Studio may rate freelancers after completed work.
- RAT-002 — Freelancer may rate studios after completed work.
- RAT-003 — Ratings shall be public on profiles.
- RAT-004 — Ratings shall not be used as an ML ranking feature.
- RAT-005 — Rating shall include a star score and optional review.
- RAT-006 — A completed work shall permit one rating per direction.

## Admin
- ADM-001 — Admin shall have a separate admin panel.
- ADM-002 — Admin login uses email and password.
- ADM-003 — Admin is monitoring/view-only for normal data.
- ADM-004 — Admin has no general edit/delete capability.
- ADM-005 — Admin may view Studio verification/identity submission status for monitoring.
- ADM-006 — No admin approval is required for Studios or Freelancers to enter the platform.

## AI/ML
- ML-001 — ML service shall use Python, FastAPI, pandas, NumPy, and scikit-learn.
- ML-002 — Model shall be a Decision Tree Regressor.
- ML-003 — Dataset shall contain approximately 5,000 logically generated synthetic records.
- ML-004 — Features shall include skill match, portfolio relevance, experience, budget compatibility, location distance, and availability/time compatibility.
- ML-005 — Ratings shall not be used as an ML feature.
- ML-006 — Training data shall be generated using explicit logical rules, not arbitrary random labels.
- ML-007 — Use train/test split and cross-validation.
- ML-008 — Evaluate with MAE, RMSE, and R².
- ML-009 — Tune tree parameters to control overfitting/underfitting.
- ML-010 — FastAPI shall expose a prediction endpoint for Spring Boot.
- ML-011 — ML output shall be a 0–100 match score.
- ML-012 — Hard availability filtering shall occur before ML ranking.
- ML-013 — Portfolio relevance shall use structured categories and metadata/text, not computer vision.

## UI/UX
- UI-001 — React + TypeScript + Tailwind CSS shall be used.
- UI-002 — Lucide React shall provide interface icons.
- UI-003 — Major forms shall use meaningful icons/visual cues.
- UI-004 — Relevant service/equipment/category selections shall use visual cards/icons/images where useful.
- UI-005 — Desktop shall use a top navigation.
- UI-006 — Mobile shall use a bottom navigation.
- UI-007 — Application shall be responsive for desktop, tablet, and mobile.
- UI-008 — Forms shall provide validation, loading, error, and success states.
- UI-009 — Freelancer profile shall present complete professional information as a strong first impression.
- UI-010 — Freelancer shall be able to edit profile information.

## AWS
- AWS-001 [TESTED] — EC2 shall use Ubuntu 24.04 LTS.
- AWS-002 [TESTED] — Development shall use VS Code Remote SSH.
- AWS-003 [IMPLEMENTED] — MySQL shall run on EC2 for this prototype.
- AWS-004 — S3 shall store profile and portfolio images.
- AWS-005 — S3 shall use logical prefixes for studio profiles, freelancer profiles, and portfolios.
- AWS-006 — S3 objects may be publicly accessible for this prototype.
- AWS-007 — Final demo shall use an Elastic IP.
- AWS-008 — Nginx shall reverse proxy the final deployed application.
- AWS-009 [TESTED] — MySQL port 3306 shall not be publicly exposed.
- AWS-010 [IMPLEMENTED] — Secrets shall be stored as environment variables.

## Documentation and quality
- DOC-001 — Code shall contain useful comments around non-obvious business logic.
- DOC-002 — README shall document setup and operation.
- DOC-003 — API documentation shall be maintained.
- DOC-004 — Database ER documentation shall be maintained.
- DOC-005 — Architecture documentation shall be maintained.
- DOC-006 — ML methodology shall be documented.
- DOC-007 — AWS deployment steps shall be documented.
- DOC-008 — Test cases shall be documented.
