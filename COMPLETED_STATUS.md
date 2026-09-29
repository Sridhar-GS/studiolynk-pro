# StudioLynk — Completed Status Log

## Phase 1 — EC2 and Repository Foundation
**Completed Date:** 2026-09-24  
**Status:** COMPLETED & TESTED

### Implemented Modules & Capabilities
1. **EC2 Environment Configuration & System Hardening**:
   - Verified Ubuntu 24.04.5 LTS runtime environment on AWS EC2 kernel `6.17.0-1017-aws`.
   - Configured and activated a persistent **2.0 GiB swapfile** (`/swapfile` with permissions `0600` added to `/etc/fstab`) to guarantee memory stability across Spring Boot JVM compilation, Node/Vite bundling, and Python microservice execution on the 3.7 GiB instance.
   - Installed **OpenJDK 21 LTS** (`21.0.12.1`) and configured Java alternatives.
   - Installed **Apache Maven 3.8.7** for Spring Boot builds and dependency management.
   - Configured official NodeSource repository and installed **Node.js v20.20.2 LTS** and **npm 10.8.2**.
   - Configured **Python 3.12.3** along with `python3-venv` and `python3-pip` (v24.0).
   - Installed **MySQL Server 8.0.46**; verified that the daemon is bound exclusively to `127.0.0.1:3306` (strictly private and not exposed to the public internet).
   - Verified that port 22 is the only listening public port and no additional public ports are exposed.

2. **Git Version Control & Repository Structure**:
   - Initialized Git repository on branch `main`.
   - Authored a comprehensive root `.gitignore` shielding `.env` files, build targets (`backend/target/`), node modules (`frontend/node_modules/`), distribution artifacts (`frontend/dist/`), python virtual environments (`ml-service/venv/`), and pycache files.
   - Authored `.env.example` defining configuration templates for MySQL, Spring Boot, JWT, Gmail SMTP, AWS S3 (`ap-south-1`), and FastAPI.
   - Committed the full architectural specification pack and Phase 1 codebase into source control.

3. **Backend Skeleton (Java 21 + Spring Boot 3.3.4)**:
   - Created `backend/pom.xml` configured for Java 21 LTS and Spring Boot 3.3.4 with `spring-boot-starter-web`, `spring-boot-starter-validation`, `spring-boot-starter-actuator`, and `spring-boot-starter-test`.
   - Implemented `com.studiolynk.StudioLynkApplication` entry point.
   - Implemented `com.studiolynk.controller.HealthController` providing a dedicated REST endpoint at `/api/health` returning JSON service status and timestamps.
   - Configured `backend/src/main/resources/application.yml` with port 8080 and actuator health probes.
   - Implemented automated Spring Boot test `com.studiolynk.StudioLynkApplicationTests` verifying application context loading and MockMvc endpoint assertions.
   - Executed `mvn clean test`: **2 of 2 tests passed successfully (0 failures, 0 errors)**.

4. **Frontend Skeleton (React 18 + TypeScript + Vite 5 + Tailwind CSS)**:
   - Configured `frontend/package.json` with React 18.3, React Router DOM 6, Tailwind CSS 3.4, and Lucide React.
   - Configured `vite.config.ts` with development proxy routes for `/api` and `/ws` pointing to the Spring Boot server.
   - Configured Tailwind CSS and PostCSS for utility-first styling.
   - Implemented `frontend/src/App.tsx` displaying an interactive, responsive foundation status page with live health status indicators for frontend, backend, and ML services.
   - Executed `npm run build`: **Compiled cleanly with TypeScript type-checking and Vite production asset bundling (0 errors)**.

5. **ML Prediction Microservice Skeleton (Python 3.12 + FastAPI + scikit-learn)**:
   - Created isolated virtual environment `ml-service/venv`.
   - Installed `fastapi`, `uvicorn`, `scikit-learn`, `pandas`, `numpy`, `pydantic`, and `httpx`.
   - Created `ml-service/app/main.py` configuring CORS middleware and a `/health` endpoint returning `{"status": "UP", "service": "studiolynk-ml-service", "version": "1.0.0"}`.
   - Verified service execution and response using FastAPI TestClient: **HTTP 200 OK with expected JSON payload**.

### Verification Summary
- **Backend**: `mvn clean test` -> `BUILD SUCCESS` (Tests run: 2, Failures: 0, Errors: 0).
- **Frontend**: `npm run build` -> `built in 4.54s` (0 TypeScript errors, production assets emitted).
- **ML Service**: TestClient check -> `RESPONSE: 200 {'status': 'UP', 'service': 'studiolynk-ml-service', 'version': '1.0.0'}`.
- **MySQL**: `systemctl status mysql` -> `active (running)`, bound to `127.0.0.1:3306`.
- **System Memory**: 3.7 GiB RAM + 2.0 GiB active swapfile.

---

## Phase 2 — Spring Boot and MySQL Foundation
**Completed Date:** 2026-09-24  
**Status:** COMPLETED & TESTED

### Implemented Modules & Capabilities
1. **MySQL Database & User Provisioning**:
   - Created database `studiolynk` with character set `utf8mb4` and collation `utf8mb4_unicode_ci`.
   - Created dedicated application database user `studiolynk_user` granted with full permissions on `studiolynk.*` exclusively over `localhost` and `127.0.0.1`.
   - Verified database connectivity over TCP socket `127.0.0.1:3306`.

2. **Flyway Migration Engine & Comprehensive Schema Initialization**:
   - Added `flyway-core` and `flyway-mysql` to `backend/pom.xml`.
   - Created `backend/src/main/resources/db/migration/V1__init_schema.sql` defining 27 normalized tables matching `docs/04-DATABASE-SPECIFICATION.md`:
     - `users`, `password_reset_otps`
     - `studios`, `studio_social_links`, `studio_identity_submissions`
     - `freelancers`, `skills`, `freelancer_skills`, `services`, `freelancer_services`, `equipment_categories`, `equipment`, `freelancer_equipment`
     - `portfolios`, `portfolio_categories`, `portfolio_images`
     - `freelancer_availability` (supporting the 10-day rolling window with AVAILABLE, BUSY, NOT_SET)
     - `work_requirements` (including protected `event_contact_name` and `event_contact_phone`), `work_requirement_skills`, `work_requirement_services`, `work_requirement_equipment`
     - `work_requests` (with status tracking: PENDING, ACCEPTED, REJECTED, CONFIRMED, CANCELLED)
     - `conversations`, `messages` (requirement-scoped chat persistence)
     - `ratings` (strictly enforcing 1-to-5 star scores and 1 rating per direction per completed work)
     - `notifications` (in-app notification inbox)
   - Seeded initial master catalogues into MySQL:
     - 10 core photography skills
     - 9 core photography services
     - 8 equipment categories
     - 17 professional camera bodies, lenses, lighting units, audio mics, and aerial drones.

3. **JPA Entity Domain Model**:
   - Implemented `BaseEntity` (`@MappedSuperclass`) handling automatic `createdAt` and `updatedAt` timestamps via `@PrePersist` and `@PreUpdate`.
   - Implemented core entities: `User`, `Studio`, `Freelancer`, `Skill`, `ServiceEntity`, `EquipmentCategory`, `Equipment`, `FreelancerAvailability`.
   - Implemented type-safe enums: `UserRole`, `AvailabilityStatus`, `RequirementStatus`, `RequestStatus`, `DayType`, `SubmissionStatus`, `RatingTargetType`.
   - Configured Hibernate with `ddl-auto: validate` to guarantee compile-time and runtime alignment between JPA entity definitions and Flyway SQL schema.

4. **Spring Data JPA Repositories**:
   - `UserRepository` (with `findByEmail`, `existsByEmail`, `findByRole`)
   - `StudioRepository` (with `findByUserId`, `existsByUserId`)
   - `FreelancerRepository` (with `findByUserId`, `existsByUserId`)
   - `SkillRepository` (with `findByNameIgnoreCase`, `findByIsCustomFalse`)
   - `ServiceRepository` (with `findByNameIgnoreCase`, `findByIsCustomFalse`)
   - `EquipmentCategoryRepository` (with `findByNameIgnoreCase`)
   - `EquipmentRepository` (with `findByCategoryId`, `findByNameIgnoreCase`)

5. **Service Layer & DTO Abstraction**:
   - `StudioService` & `StudioServiceImpl`
   - `FreelancerService` & `FreelancerServiceImpl`
   - `CatalogueService` & `CatalogueServiceImpl`
   - Data Transfer Objects: `UserSummaryDto`, `StudioSummaryDto`, `FreelancerSummaryDto`, and `ApiResponse<T>` response envelope.

6. **Global Exception Handling & Validation Framework**:
   - Implemented custom exceptions: `ResourceNotFoundException` (404), `BadRequestException` (400), `DuplicateResourceException` (409).
   - Created `GlobalExceptionHandler` with `@RestControllerAdvice` converting all unchecked errors and validation failures (`MethodArgumentNotValidException`) into structured `ApiError` payloads.

7. **Basic API Documentation (Swagger / OpenAPI 3)**:
   - Added `springdoc-openapi-starter-webmvc-ui` (v2.6.0).
   - Created `OpenApiConfig` configuring StudioLynk API metadata.
   - Available via `/v3/api-docs` and `/swagger-ui.html`.

8. **REST Controllers**:
   - `CatalogueController` (`/api/skills`, `/api/services`, `/api/equipment/categories`, `/api/equipment`)
   - `StudioController` (`/api/studios`, `/api/studios/{id}`)
   - `FreelancerController` (`/api/freelancers`, `/api/freelancers/{id}`)

### Verification Summary
- **Backend Tests**: `mvn test` -> **5 of 5 tests PASSED (0 failures, 0 errors, 0 skipped)** in 18.2s.
  1. `contextLoads`: Spring Boot application context and Flyway schema verification.
  2. `healthEndpointReturnsUp`: `/api/health` returns status UP.
  3. `catalogueSkillsReturnsSeededData`: `/api/skills` retrieves Flyway seed data from MySQL.
  4. `testUserPersistence`: Spring Data JPA persists and queries `User` entity with auditing timestamps.
  5. `openApiDocsAccessible`: `/v3/api-docs` emits valid OpenAPI JSON specification.
- **Frontend Build**: `npm run build` -> **built in 4.56s (0 errors)**.
- **Database Schema**: 27 tables verified in MySQL with all FK constraints and indexes active.

---

## Phase 3 — Authentication & Security Foundation
**Completed Date:** 2026-09-24  
**Status:** COMPLETED & TESTED

### Implemented Modules & Capabilities
1. **Spring Security & JJWT Architecture**:
   - Added `spring-boot-starter-security`, `spring-boot-starter-mail`, and `jjwt-api`, `jjwt-impl`, `jjwt-jackson` (v0.12.6) to `backend/pom.xml`.
   - Implemented `JwtTokenProvider` generating HMAC-SHA256 signed tokens containing subject (email), user ID, and role claim with a 24-hour expiration window.
   - Implemented `CustomUserDetailsService` resolving users from MySQL and mapping role into Spring Security granted authorities (`ROLE_STUDIO`, `ROLE_FREELANCER`, `ROLE_ADMIN`).
   - Implemented `JwtAuthenticationFilter` intercepting incoming HTTP requests, extracting `Authorization: Bearer <token>`, validating claims, and populating `SecurityContextHolder`.
   - Implemented `JwtAuthenticationEntryPoint` returning structured JSON 401 Unauthorized responses matching the standard `ApiError` format.
   - Configured `SecurityConfig` with `BCryptPasswordEncoder`, stateless session creation policy (`SessionCreationPolicy.STATELESS`), CORS configuration, and explicit request authorization rules protecting all platform resources while keeping register, login, forgot-password, health, and OpenAPI docs accessible.

2. **User Registration & Role Selection (AUTH-001, ONB-001, AUTH-007)**:
   - Implemented `AuthService` and `AuthServiceImpl` with BCrypt password hashing.
   - Enforced password complexity verification matching AUTH-007: minimum 8 characters, at least 1 uppercase letter, at least 1 lowercase letter, at least 1 number, and at least 1 special character.
   - Handled role specification at registration (`STUDIO` or `FREELANCER`) per ONB-001, persisting the user in MySQL with `onboardingCompleted = false`.
   - Prevented duplicate user registration with `DuplicateResourceException` (HTTP 409).
   - Automatically issued a signed JWT token upon registration so the user seamlessly transitions into onboarding.

3. **Single Login Portal & Role-Based Routing (AUTH-002)**:
   - Implemented `POST /api/auth/login` validating credentials against BCrypt hashes.
   - Returns `AuthResponseDto` containing JWT token and `UserSummaryDto` with `userId`, `email`, `role`, and `onboardingCompleted`.
   - Implemented `GET /api/auth/me` allowing frontend clients to refresh session state and verify JWT token validity on page reload.

4. **OTP-Based Password Reset via SMTP (AUTH-003, AUTH-004, AUTH-005, AUTH-006)**:
   - Avoided Firebase completely in adherence to AUTH-004.
   - Implemented `PasswordResetOtp` entity mapped to MySQL table `password_reset_otps`.
   - Generated cryptographically secure 6-digit numeric OTPs.
   - Configured strict 5-minute OTP lifetime window (AUTH-005).
   - Enforced maximum 3 failed verification attempts before the OTP is invalidated (AUTH-006).
   - Enforced 60-second cooldown period between successive OTP requests to prevent abuse (AUTH-006).
   - Implemented `EmailService` utilizing `JavaMailSender` with a graceful dev fallback that logs the OTP code to server output when live SMTP credentials are not yet configured.
   - Implemented `POST /api/auth/forgot-password`, `POST /api/auth/verify-reset-otp`, and `POST /api/auth/reset-password` endpoints.

5. **Onboarding Access Gate (ONB-001, ONB-002, ONB-004)**:
   - Implemented `OnboardingController` (`GET /api/onboarding/status`) checking whether the logged-in user has completed onboarding for their role.
   - Incomplete accounts cannot access main platform routes and are gated to `/onboarding`.
   - Accounts can safely log out and resume onboarding later upon logging back in (ONB-004).

6. **React Frontend Authentication & UI Experience**:
   - `frontend/src/services/api.ts`: Configured Axios interceptors to automatically attach JWT Bearer tokens and clear local state upon 401 responses.
   - `frontend/src/context/AuthContext.tsx`: Full React Context providing `user`, `token`, `login`, `register`, `logout`, `refreshUser`, and automatic session hydration from `/api/auth/me`.
   - `frontend/src/components/common/Navbar.tsx`: Photography-themed navigation header with role badges, onboarding indicator, and sign-out action.
   - `frontend/src/components/common/ProtectedRoute.tsx`: Route guard enforcing authentication and onboarding completion requirements.
   - `frontend/src/pages/auth/LoginPage.tsx`: Single login form with password visibility toggle, error handling, and role/onboarding-based navigation.
   - `frontend/src/pages/auth/RegisterPage.tsx`: Interactive visual cards for role selection (`STUDIO` vs `FREELANCER`), email input, and real-time password requirement checklist (AUTH-007).
   - `frontend/src/pages/auth/ForgotPasswordPage.tsx`: 3-step interactive wizard (request OTP -> verify 6-digit OTP with 5-min timer and 60s cooldown -> set new strong password -> success confirmation).
   - `frontend/src/pages/onboarding/OnboardingPendingPage.tsx`: Dedicated gate view informing users of pending onboarding requirements for their specific role with save & resume later support.
   - `frontend/src/pages/HomePage.tsx` and `frontend/src/App.tsx`: Fully wired client SPA with React Router.

### Verification Summary
- **Backend Test Suite**: `mvn test` -> **14 of 14 tests PASSED (0 failures, 0 errors, 0 skipped)** in 21.7s:
  1. `StudioLynkApplicationTests.contextLoads`
  2. `StudioLynkApplicationTests.healthEndpointReturnsUp`
  3. `StudioLynkApplicationTests.catalogueSkillsReturnsSeededData`
  4. `StudioLynkApplicationTests.testUserPersistence`
  5. `StudioLynkApplicationTests.openApiDocsAccessible`
  6. `AuthControllerTests.registerStudioSuccessfully`
  7. `AuthControllerTests.registerFreelancerSuccessfully`
  8. `AuthControllerTests.registerDuplicateEmailFails`
  9. `AuthControllerTests.registerWeakPasswordFails`
  10. `AuthControllerTests.loginSuccessReturnsJwtToken`
  11. `AuthControllerTests.loginInvalidPasswordFails`
  12. `AuthControllerTests.getCurrentUserWithToken`
  13. `AuthControllerTests.getCurrentUserWithoutTokenFails`
  14. `AuthControllerTests.forgotPasswordAndResetFlow`
- **Frontend Build**: `npm run build` -> **Compiled cleanly with TypeScript type-checking and Vite production asset bundling in 5.34s (0 errors)**.

---

## Phase 4 — Studio Onboarding & Profile
**Completed Date:** 2026-09-24  
**Status:** COMPLETED & TESTED

### Implemented Modules & Capabilities
1. **JPA Domain Entities & Database Mapping**:
   - Implemented `StudioSocialLink` entity mapped to MySQL table `studio_social_links`.
   - Implemented `StudioIdentitySubmission` entity mapped to MySQL table `studio_identity_submissions` with `SubmissionStatus` (`SUBMITTED`, `VERIFIED`).
   - Enhanced `Studio` entity with `@OneToMany` cascades for `socialLinks` and `identitySubmissions`.
   - Created `StudioSocialLinkRepository` and `StudioIdentitySubmissionRepository`.

2. **Data Transfer Objects (DTOs)**:
   - `StudioOnboardingRequestDto`: Collects studio name, owner name, phone, address, latitude, longitude, years of operation, logo URL, social links, document type, document reference, and owner legal declaration text (STU-001, STU-002).
   - `StudioUpdateRequestDto`: Dedicated payload for editing studio profile details (STU-005).
   - `StudioProfileDto`: Complete profile representation returning user details, coordinates, social links, identity declaration, and live profile completion percentage (ONB-005).
   - `StudioSocialLinkDto` & `StudioIdentitySubmissionDto`.

3. **Service Layer Architecture**:
   - `StudioService` and `StudioServiceImpl`:
     - `saveOrUpdateOnboarding`: Supports final onboarding completion (marking `user.onboardingCompleted = true` and unlocking the platform per STU-004) or saving an in-progress draft without completing (ONB-004).
     - Role restriction: Validates that only users with role `STUDIO` can submit studio onboarding.
     - STU-003 Compliance: Prototype identity declaration without external Aadhaar integration or Aadhaar number persistence.
     - `getStudioProfileByEmail` / `getStudioProfileById`: Delivers complete profile data including social links and identity declarations.
     - `updateStudioProfile`: Enables updating studio name, owner name, phone, address, coordinates, years in business, logo, and social links (STU-005).
     - `calculateCompletionPercentage`: Computes deterministic health score (0–100%) based on filled attributes (ONB-005).

4. **REST Endpoints**:
   - `POST /api/onboarding/studio`: Submit final studio onboarding & unlock platform access (STU-001, STU-004).
   - `PUT /api/onboarding/studio`: Save draft onboarding without completing (ONB-004).
   - `GET /api/studios/me`: Get current authenticated studio profile and completion metrics.
   - `PUT /api/studios/me`: Update studio profile details (STU-005).
   - `GET /api/studios/{id}`: View studio profile by ID.
   - `GET /api/studios`: List all registered studios.

5. **React Frontend Pages & Navigation**:
   - `StudioOnboardingPage.tsx`: Interactive multi-section wizard featuring 4 distinct cards (Business Identity, Location & Coordinates with quick presets for Chennai, Bangalore, Mumbai, Hyderabad, Delhi, Kochi, Social Links, and Owner Identity Declaration), live profile completion percentage meter (ONB-005), Save Draft button (ONB-004), and Complete Onboarding submission (STU-004).
   - `StudioProfilePage.tsx`: Rich profile layout displaying studio header, verified badge, completion meter, phone/email/address/coordinate cards, clickable social links, owner identity verification status, and an inline edit form (STU-005).
   - `StudioDashboardPage.tsx`: Studio dashboard with operational stats, profile status, and quick workflow cards for Find Freelancers, Work Requirements, and Messages.
   - `Navbar.tsx`: Updated with role-aware Studio Dashboard and Profile navigation links.
   - `App.tsx`: Wired protected routes for `/onboarding/studio`, `/studio/dashboard`, `/studio/profile`.

### Verification Summary
- **Backend Test Suite**: `mvn test` -> **18 of 18 tests PASSED (0 failures, 0 errors, 0 skipped)** in 23.4s:
  1. `StudioLynkApplicationTests.contextLoads`
  2. `StudioLynkApplicationTests.healthEndpointReturnsUp`
  3. `StudioLynkApplicationTests.catalogueSkillsReturnsSeededData`
  4. `StudioLynkApplicationTests.testUserPersistence`
  5. `StudioLynkApplicationTests.openApiDocsAccessible`
  6. `AuthControllerTests.registerStudioSuccessfully`
  7. `AuthControllerTests.registerFreelancerSuccessfully`
  8. `AuthControllerTests.registerDuplicateEmailFails`
  9. `AuthControllerTests.registerWeakPasswordFails`
  10. `AuthControllerTests.loginSuccessReturnsJwtToken`
  11. `AuthControllerTests.loginInvalidPasswordFails`
  12. `AuthControllerTests.getCurrentUserWithToken`
  13. `AuthControllerTests.getCurrentUserWithoutTokenFails`
  14. `AuthControllerTests.forgotPasswordAndResetFlow`
  15. `StudioControllerTests.testStudioOnboardingSuccess` (STU-001, STU-004)
  16. `StudioControllerTests.testStudioOnboardingDraft` (ONB-004)
  17. `StudioControllerTests.testNonStudioUserCannotSubmitStudioOnboarding`
  18. `StudioControllerTests.testGetAndEditStudioProfile` (STU-005)
- **Frontend Build**: `npm run build` -> **Compiled cleanly with TypeScript type-checking and Vite production asset bundling in 5.83s (0 errors)**.

---

## Phase 5 — Freelancer Onboarding & Profile
**Completed Date:** 2026-09-24  
**Status:** COMPLETED & TESTED

### Implemented Modules & Capabilities
1. **JPA Domain Entities & Database Mappings**:
   - Enhanced `Freelancer` entity with `@ManyToMany` relationships:
     - `skills` mapped via MySQL join table `freelancer_skills` (`freelancer_id`, `skill_id`).
     - `services` mapped via MySQL join table `freelancer_services` (`freelancer_id`, `service_id`).
     - `equipment` mapped via MySQL join table `freelancer_equipment` (`freelancer_id`, `equipment_id`).
   - Implemented bidirectional entity methods and transactional persistence.

2. **Data Transfer Objects (DTOs)**:
   - `CustomItemRequestDto` & `CustomEquipmentRequestDto`: Dynamic catalogue additions (FRL-002, FRL-003, FRL-005).
   - `SkillDto`, `ServiceDto`, `EquipmentDto`: Domain transfer objects with custom indicator flags.
   - `FreelancerOnboardingRequestDto`: Full onboarding payload capturing full name, phone, address, coordinates, experience years, bio, photo URL, full/half-day rates, and selection ID lists for skills, services, and equipment (FRL-001).
   - `FreelancerUpdateRequestDto`: Dedicated profile editing payload (FRL-006).
   - `FreelancerProfileDto`: Full representation returning creator identity, contact, rates, category-grouped equipment, skills, services, timestamps, and live completion score (ONB-005).

3. **Catalogue Extensions & Custom Additions (FRL-002, FRL-003, FRL-005)**:
   - `CatalogueService` & `CatalogueServiceImpl`: Added `createCustomSkill`, `createCustomService`, and `createCustomEquipment` marking `is_custom = true` and linking creator `user_id`.
   - `CatalogueController`:
     - `POST /api/skills/custom`: Add custom skill (FRL-002).
     - `POST /api/services/custom`: Add custom service (FRL-003).
     - `POST /api/equipment/custom`: Add custom equipment gear under a selected category (FRL-005).

4. **Freelancer Service Layer & Business Logic**:
   - `FreelancerService` and `FreelancerServiceImpl`:
     - `saveOrUpdateOnboarding`: Role-restricted (`FREELANCER` only), manages Many-to-Many associations, validates rates and contact information, and activates the account upon final submission (`user.onboardingCompleted = true`).
     - FRL-007 Compliance: Instant platform unlock upon submission with zero admin verification required.
     - Draft persistence (ONB-004): Allows saving progress without unlocking the platform.
     - `getFreelancerProfileByEmail` / `getFreelancerProfileById`: Delivers complete profile data including skills, services, and gear.
     - `updateFreelancerProfile`: Enables modifying professional profile attributes, rates, skills, services, and equipment (FRL-006).
     - `calculateCompletionPercentage`: Computes deterministic score (0–100%) matching ONB-005 formula: Full Name (15%), Phone (15%), Address (10%), Coordinates (10%), Experience (10%), Rates (10%), Skills (10%), Services (10%), Equipment (5%), Bio (5%).

5. **REST Endpoints**:
   - `POST /api/onboarding/freelancer`: Submit final freelancer onboarding and unlock platform access (ONB-002, ONB-003, FRL-007).
   - `PUT /api/onboarding/freelancer`: Save freelancer onboarding draft without completing (ONB-004).
   - `GET /api/freelancers/me`: Retrieve authenticated freelancer profile and completion score (FRL-006).
   - `PUT /api/freelancers/me`: Update authenticated freelancer profile details (FRL-006).
   - `GET /api/freelancers/{id}`: View freelancer profile by ID.
   - `GET /api/freelancers`: List all registered freelancers.

6. **React Frontend Pages & Navigation**:
   - `types/index.ts`: TypeScript interfaces for `Skill`, `ServiceItem`, `EquipmentCategory`, `EquipmentItem`, `FreelancerProfile`, and onboarding/update payloads.
   - `services/catalogueService.ts`: Reusable API client for catalogue data and custom additions.
   - `services/freelancerService.ts`: Reusable API client for onboarding, draft persistence, profile fetching, and profile updates.
   - `FreelancerOnboardingPage.tsx`: Interactive multi-section wizard:
     - Creator Details & Location with City GPS Presets (Chennai, Bangalore, Mumbai, Hyderabad, Delhi, Kochi).
     - Photography Skills with custom skill modal (FRL-002).
     - Services Offered with custom service modal (FRL-003).
     - Camera Gear & Equipment grouped by category (Cameras, Lenses, Lighting, Drones, Audio, Accessories) with custom gear modal (FRL-004, FRL-005).
     - Full-day & Half-day pricing rates (FRL-001).
     - Dynamic profile completion meter (ONB-005).
     - Save Draft button (ONB-004) and Complete Profile & Launch button (ONB-002, FRL-007).
   - `FreelancerProfilePage.tsx`: Rich profile layout displaying avatar, verified badge, completion meter, contact details, bio, skills chips, services badges, categorized gear, and inline edit modal/form (FRL-006).
   - `FreelancerDashboardPage.tsx`: Overview dashboard with rate cards, active skills/gear counts, profile shortcuts, and roadmap teasers for Portfolio (Phase 6), Availability (Phase 8), and Bookings (Phase 9).
   - `Navbar.tsx`: Updated with Freelancer Dashboard and Profile links, Phase 5 Active badge, and role-based onboarding redirection.
   - `OnboardingPendingPage.tsx`: Updated with automatic redirection to `/onboarding/freelancer` for freelancer accounts.
   - `App.tsx`: Wired protected routes for `/onboarding/freelancer`, `/freelancer/dashboard`, `/freelancer/profile`.

### Verification Summary
- **Backend Test Suite**: `mvn test` -> **23 of 23 tests PASSED (0 failures, 0 errors, 0 skipped)** in 27.5s:
  1. `StudioLynkApplicationTests.contextLoads`
  2. `StudioLynkApplicationTests.healthEndpointReturnsUp`
  3. `StudioLynkApplicationTests.catalogueSkillsReturnsSeededData`
  4. `StudioLynkApplicationTests.testUserPersistence`
  5. `StudioLynkApplicationTests.openApiDocsAccessible`
  6. `AuthControllerTests.registerStudioSuccessfully`
  7. `AuthControllerTests.registerFreelancerSuccessfully`
  8. `AuthControllerTests.registerDuplicateEmailFails`
  9. `AuthControllerTests.registerWeakPasswordFails`
  10. `AuthControllerTests.loginSuccessReturnsJwtToken`
  11. `AuthControllerTests.loginInvalidPasswordFails`
  12. `AuthControllerTests.getCurrentUserWithToken`
  13. `AuthControllerTests.getCurrentUserWithoutTokenFails`
  14. `AuthControllerTests.forgotPasswordAndResetFlow`
  15. `StudioControllerTests.testStudioOnboardingSuccess` (STU-001, STU-004)
  16. `StudioControllerTests.testStudioOnboardingDraft` (ONB-004)
  17. `StudioControllerTests.testNonStudioUserCannotSubmitStudioOnboarding`
  18. `StudioControllerTests.testGetAndEditStudioProfile` (STU-005)
  19. `FreelancerControllerTests.testFreelancerOnboardingSuccess` (FRL-001, FRL-007, ONB-002)
  20. `FreelancerControllerTests.testFreelancerOnboardingDraft` (ONB-004)
  21. `FreelancerControllerTests.testNonFreelancerUserCannotSubmitFreelancerOnboarding`
  22. `FreelancerControllerTests.testCreateCustomCatalogueItems` (FRL-002, FRL-003, FRL-005)
  23. `FreelancerControllerTests.testGetAndEditFreelancerProfile` (FRL-006)
- **Frontend Build**: `npm run build` -> **Compiled cleanly with TypeScript type-checking and Vite production asset bundling in 6.40s (0 errors)**.

---

## Phase 6 — Portfolio and AWS S3 Storage
**Completed Date:** 2026-09-24  
**Status:** COMPLETED & TESTED

### Implemented Modules & Capabilities
1. **JPA Domain Entities & Database Mappings**:
   - `Portfolio` (`portfolios` table): Extends `BaseEntity` with auditing timestamps, `@OneToOne` with `Freelancer` (unique constraint), and `@OneToMany` with `PortfolioCategory` (cascade all, orphan removal).
   - `PortfolioCategory` (`portfolio_categories` table): Linked via `@ManyToOne` to `Portfolio`, holds `name`, `sortOrder`, `createdAt`, and `@OneToMany` with `PortfolioImage` (cascade all, orphan removal).
   - `PortfolioImage` (`portfolio_images` table): Linked via `@ManyToOne` to `PortfolioCategory`, records `s3Key`, `imageUrl`, `originalFilename`, `contentType`, `fileSize`, `sortOrder`, and `createdAt`.
   - Created `PortfolioRepository`, `PortfolioCategoryRepository`, and `PortfolioImageRepository`.

2. **Pluggable Storage Architecture (S3 + Local Fallback)**:
   - `StorageService` interface defining `uploadFile`, `deleteFile`, and `getFileUrl`.
   - `S3StorageServiceImpl`: Production implementation utilizing AWS SDK v2 (`software.amazon.awssdk:s3:2.25.16`), generating canonical S3 URLs (`https://{bucket}.s3.{region}.amazonaws.com/{key}`).
   - `LocalStorageServiceImpl`: Seamless fallback for local development and offline environments, writing files to `./uploads` and providing accessible `/api/uploads/{key}` URLs.
   - `StorageConfig`: Spring `@Configuration` that detects AWS credentials and bucket configuration dynamically, falling back gracefully to local storage when credentials are not configured, and registering a `ResourceHandler` to serve local uploads.

3. **Data Transfer Objects (DTOs)**:
   - `PortfolioDto`, `PortfolioCategoryDto`, `PortfolioImageDto`: Structured representations with image counts, nested lists, and timestamps.
   - `CreateCategoryRequestDto` & `UpdateCategoryRequestDto`: Validated category management payloads.
   - `ReorderItemsRequestDto`: Generic list-based ID sequence payload for image and category reordering.
   - `UploadResponseDto`: Standardized file upload response containing `s3Key`, `imageUrl`, `originalFilename`, `contentType`, and `fileSize`.

4. **Portfolio Service Layer & Business Rules**:
   - `PortfolioService` and `PortfolioServiceImpl`:
     - `getOrCreatePortfolio`: Auto-initializes portfolio for authenticated freelancer (POR-001).
     - `getPortfolioByFreelancerId`: Public/studio read access (POR-007).
     - `createCategory`, `updateCategory`, `deleteCategory`: Full category CRUD with automatic cascading S3 file cleanup (POR-002, POR-003, POR-005).
     - `uploadImages`: Multi-file upload validating MIME types (`image/jpeg`, `image/png`, `image/webp`) and file size (max 10MB) per POR-004 & POR-008. Formats S3 keys matching `portfolio/{freelancerId}/{categoryId}/{uuid}_{filename}`.
     - `deleteImage`: Deletes image from storage and database (POR-005).
     - `reorderImages`: Interactive drag/drop sort order reindexing within a category (POR-006).
     - `reorderCategories`: Sort order reindexing of categories.
     - `uploadProfileImage`: Handles studio logos and freelancer profile avatars.

5. **REST Endpoints**:
   - `GET /api/portfolio/me`: Authenticated freelancer portfolio.
   - `GET /api/portfolio/freelancer/{freelancerId}`: Public/studio viewing.
   - `POST /api/portfolio/categories`: Create category.
   - `PUT /api/portfolio/categories/{categoryId}`: Edit category.
   - `DELETE /api/portfolio/categories/{categoryId}`: Delete category & its images.
   - `POST /api/portfolio/categories/{categoryId}/images`: Upload images (`multipart/form-data`).
   - `DELETE /api/portfolio/images/{imageId}`: Delete image.
   - `PUT /api/portfolio/categories/{categoryId}/reorder`: Reorder images.
   - `PUT /api/portfolio/categories/reorder`: Reorder categories.
   - `POST /api/upload/image`: General profile photo and studio logo uploads.

6. **React Frontend Pages & Navigation**:
   - `types/index.ts`: TypeScript interfaces for `Portfolio`, `PortfolioCategory`, `PortfolioImage`, and payloads.
   - `services/portfolioService.ts`: Reusable API client for all portfolio and upload operations with `FormData` multipart support.
   - `FreelancerPortfolioPage.tsx`: Interactive portfolio workspace with category tabs, Add/Rename/Delete category modals, multi-file drag-and-drop upload zone, image grid cards with Move Left / Move Right reordering controls (POR-006), and full-screen lightbox preview.
   - `FreelancerProfilePage.tsx`: Integrated link to manage portfolio showcase.
   - `FreelancerDashboardPage.tsx`: Activated the Portfolio Showcase card linking directly to `/freelancer/portfolio`.
   - `Navbar.tsx`: Added "Portfolio (S3)" link for authenticated freelancers and updated badge to "Phase 6 Active".
   - `App.tsx`: Added protected route `/freelancer/portfolio`.

### Verification Summary
- **Backend Test Suite**: `mvn test` -> **28 of 28 tests PASSED (0 failures, 0 errors, 0 skipped)** in 31.5s:
  1. `StudioLynkApplicationTests.contextLoads`
  2. `StudioLynkApplicationTests.healthEndpointReturnsUp`
  3. `StudioLynkApplicationTests.catalogueSkillsReturnsSeededData`
  4. `StudioLynkApplicationTests.testUserPersistence`
  5. `StudioLynkApplicationTests.openApiDocsAccessible`
  6. `AuthControllerTests.registerStudioSuccessfully`
  7. `AuthControllerTests.registerFreelancerSuccessfully`
  8. `AuthControllerTests.registerDuplicateEmailFails`
  9. `AuthControllerTests.registerWeakPasswordFails`
  10. `AuthControllerTests.loginSuccessReturnsJwtToken`
  11. `AuthControllerTests.loginInvalidPasswordFails`
  12. `AuthControllerTests.getCurrentUserWithToken`
  13. `AuthControllerTests.getCurrentUserWithoutTokenFails`
  14. `AuthControllerTests.forgotPasswordAndResetFlow`
  15. `StudioControllerTests.testStudioOnboardingSuccess`
  16. `StudioControllerTests.testStudioOnboardingDraft`
  17. `StudioControllerTests.testNonStudioUserCannotSubmitStudioOnboarding`
  18. `StudioControllerTests.testGetAndEditStudioProfile`
  19. `FreelancerControllerTests.testFreelancerOnboardingSuccess`
  20. `FreelancerControllerTests.testFreelancerOnboardingDraft`
  21. `FreelancerControllerTests.testNonFreelancerUserCannotSubmitFreelancerOnboarding`
  22. `FreelancerControllerTests.testCreateCustomCatalogueItems`
  23. `FreelancerControllerTests.testGetAndEditFreelancerProfile`
  24. `PortfolioControllerTests.testGetOrCreatePortfolio` (POR-001)
  25. `PortfolioControllerTests.testCategoryCrudOperations` (POR-002, POR-003, POR-005)
  26. `PortfolioControllerTests.testImageUploadReorderAndDelete` (POR-004, POR-006, POR-008)
  27. `PortfolioControllerTests.testPublicPortfolioViewing` (POR-007)
  28. `PortfolioControllerTests.testProfileImageUpload`
- **Frontend Build**: `npm run build` -> **Compiled cleanly with TypeScript type-checking and Vite production asset bundling in 6.07s (0 errors)**.

---

## Phase 7 — Availability Calendar & Shoot Filtering
**Completed Date:** 2026-09-28  
**Status:** COMPLETED & TESTED

### Implemented Modules & Capabilities
1. **Repository & Domain Mapping**:
   - `FreelancerAvailabilityRepository`: Added queries for finding slots by freelancer ID and date ranges, single date lookups, range deletions, and status existence checks.
   - Leveraged existing Flyway V1 `freelancer_availability` schema with foreign key cascading and `(freelancer_id, available_date)` unique constraint.

2. **Data Transfer Objects (DTOs)**:
   - `AvailabilitySlotDto`: Represents single calendar slot in the rolling 10-day window (`date`, `dayOfWeek`, `dayOfMonth`, `month`, `status`, `startTime`, `endTime`, `formattedTime`, `withinWindow`, `available`).
   - `AvailabilityWindowResponseDto`: Represents the complete 10-day consecutive sequence (`freelancerId`, `freelancerName`, `windowStartDate`, `windowEndDate`, `totalDays`, `availableDaysCount`, `busyDaysCount`, `notSetDaysCount`, `slots`).
   - `UpdateAvailabilityItemDto` & `UpdateAvailabilityRequestDto`: Validated batch update payloads requiring valid start/end times when marked `AVAILABLE`.
   - `AvailabilityCheckRequestDto` & `AvailabilityCheckResponseDto`: Detailed shoot query payload and response indicating `match`, `withinWindow`, `status`, shoot hours, and human-readable explanation reason.

3. **Service Layer & Business Rules (AVL-001 - AVL-009)**:
   - `AvailabilityService` & `AvailabilityServiceImpl`:
     - **Rolling 10-Day Window (AVL-001)**: Dynamic sequence from `LocalDate.now()` through `LocalDate.now().plusDays(9)`. Synthesizes unconfigured days as `NOT_SET` with null times.
     - **Status Management (AVL-002, AVL-003)**: Validates start time before end time when `AVAILABLE`. Clears times when marked `BUSY` or `NOT_SET`. Rejects updates to past dates or dates beyond the 10-day window.
     - **Reset Capability**: Resets all slots within the 10-day window back to `NOT_SET`.
     - **Studio Shoot Filtering (AVL-004, AVL-005)**: Checks candidate availability for requested dates and time intervals. Rejects `BUSY` and `NOT_SET` candidates. Verifies that freelancer available hours cover requested shoot interval.
     - **Beyond 10-Day Window (AVL-006)**: Accurately flags queries beyond 10 days with `withinWindow = false`, `status = NOT_SET`, and explanatory note that availability is unknown.
     - **Hard Filtering & Conflict Foundation (AVL-007, AVL-008, AVL-009)**: `filterAvailableFreelancerIds()` filters candidate freelancer IDs for Phase 8 Discovery and Phase 15 ML ranking; `hasConflict()` checks interval overlaps for work booking confirmation.

4. **REST Endpoints**:
   - `GET /api/freelancers/me/availability`: Current freelancer's rolling 10-day availability.
   - `PUT /api/freelancers/me/availability`: Update availability slots with status and hours.
   - `POST /api/freelancers/me/availability/reset`: Reset 10-day window to `NOT_SET`.
   - `GET /api/freelancers/{id}/availability`: Public/studio viewing of any freelancer's 10-day availability window.
   - `POST /api/freelancers/availability/check`: Shoot slot check via request body.
   - `GET /api/freelancers/availability/check`: Shoot slot check via query parameters (`date`, `startTime`, `endTime`).

5. **React Frontend Pages & Navigation**:
   - `types/index.ts`: Added `AvailabilityStatus`, `AvailabilitySlot`, `AvailabilityWindowResponse`, `UpdateAvailabilityItem`, `UpdateAvailabilityPayload`, `AvailabilityCheckRequest`, `AvailabilityCheckResponse`.
   - `services/availabilityService.ts`: Reusable API client for all availability operations.
   - `FreelancerAvailabilityPage.tsx`: Interactive 10-day calendar workspace with:
     - Header displaying active rolling window date range.
     - Informational rules callout explaining Available, Busy, and Beyond 10-Day behavior.
     - Live statistics summary bar (Available, Busy, Not Set counts).
     - Bulk quick-action buttons ("Quick: All Available", "Weekdays Open, Weekends Busy", "Reset to Not Set").
     - 10 day cards with relative day tags ("Today", "Tomorrow", "Day 3"...), status toggles (`Available`, `Busy`, `Not Set`), time pickers, and quick preset hours chips ("Full Day 09:00 - 18:00", "Morning 09:00 - 13:00", "Afternoon 14:00 - 19:00", "Night 17:00 - 22:00").
     - Sticky floating save bar with dirty state tracking.
   - `FreelancerProfilePage.tsx`: Integrated live 10-day calendar preview widget and link to manage availability.
   - `FreelancerDashboardPage.tsx`: Activated Card 3 ("10-Day Availability Calendar") with direct link to `/freelancer/availability`.
   - `Navbar.tsx`: Added "Availability" link with Calendar icon for authenticated Freelancers and updated badge to "Phase 7 Active".
   - `App.tsx`: Added protected route `/freelancer/availability`.

### Verification Summary
- **Backend Test Suite**: `mvn test` -> **34 of 34 tests PASSED (0 failures, 0 errors, 0 skipped)** in 34.9s:
  1. `StudioLynkApplicationTests` (5 tests)
  2. `AuthControllerTests` (9 tests)
  3. `StudioControllerTests` (4 tests)
  4. `FreelancerControllerTests` (5 tests)
  5. `PortfolioControllerTests` (5 tests)
  6. `FreelancerAvailabilityControllerTests` (6 tests):
     - `testGetMyAvailability_returns10DayWindowDefaultNotSet` (AVL-001)
     - `testUpdateMyAvailability_success` (AVL-002, AVL-003)
     - `testUpdateMyAvailability_validationErrors` (AVL-003)
     - `testCheckAvailability_matchingRules` (AVL-004, AVL-005, AVL-006)
     - `testResetMyAvailability_success`
     - `testGetFreelancerAvailability_byId`
- **Frontend Build**: `npm run build` -> **Compiled cleanly with TypeScript type-checking and Vite production asset bundling in 6.57s (0 errors)**.

---

## Phase 8 — Freelancer Discovery & Multi-Parameter Search
**Completed Date:** 2026-09-28  
**Status:** COMPLETED & TESTED

### Implemented Modules & Capabilities
1. **Data Transfer Objects (DTOs)**:
   - `FreelancerSearchFilterDto`: Comprehensive multi-parameter filter model supporting `keyword` (full text across name, bio, address, skills, services, gear), `serviceId` / `serviceName`, `skillId` / `skillName`, `equipmentId` / `equipmentName`, `location` (text string), `latitude` & `longitude` (for Haversine distance), `maxDistanceKm`, `date`, `startTime`, `endTime`, `dayType` (`FULL_DAY` vs `HALF_DAY`), `maxBudget`, `minExperience`, and `sortBy` (`relevance`, `distance`, `rating`, `experience`, `price_asc`, `price_desc`).
   - `FreelancerCardDto`: Rich presentation model for discovery results (DIS-004, DIS-005) including `id`, `userId`, `fullName`, `profilePhotoUrl`, `phone`, `address`, `latitude`, `longitude`, `distanceKm`, `formattedDistance`, `experienceYears`, `bio`, `fullDayRate`, `halfDayRate`, `averageRating`, `reviewCount`, `skills`, `services`, `equipment`, `primaryRole`, `availabilityStatus`, `availableHours`, `withinWindow`, `availabilityNotice`, `aiMatchScore` (DIS-006), and `onboardingCompleted`.
   - `FreelancerSearchResponseDto`: Search result envelope containing `totalResults`, `searchedDate`, `searchedTime`, `searchedLocation`, and list of `freelancers`.

2. **Backend Service & Algorithm Implementation**:
   - `FreelancerService` & `FreelancerServiceImpl`:
     - **Onboarding Gate (DIS-001)**: Strictly filters out freelancers whose onboarding is incomplete (`f.getUser().isOnboardingCompleted() == true`), ensuring incomplete drafts are never exposed to studios.
     - **Multi-Parameter Search Engine (DIS-003)**:
       - Multi-field keyword search across name, bio, address, skills, services, and equipment.
       - Exact or name-based filtering on services, skills, and equipment.
       - Minimum experience years filter.
       - Day-type sensitive budget filter (evaluates `halfDayRate` for `HALF_DAY` and `fullDayRate` for `FULL_DAY`).
       - Case-insensitive location text filtering.
     - **Haversine Great-Circle Distance Algorithm**: Calculates exact spherical distance in kilometers using Earth radius $R = 6371.0\text{ km}$ based on studio latitude/longitude and freelancer coordinates. Filters by `maxDistanceKm` radius and formats distance string.
     - **Rolling 10-Day Availability Hard Filtering (AVL-004, AVL-005, AVL-006)**:
       - Seamlessly integrated with `AvailabilityService.checkAvailability()`.
       - For dates within the 10-day window: candidates marked `BUSY`, `NOT_SET`, or with non-covering hours are strictly excluded from search results (`AVL-004`, `AVL-005`).
       - For dates beyond the 10-day window: candidates remain visible with `withinWindow = false`, `availabilityStatus = NOT_SET`, and explanatory notice: *"Beyond 10-day scheduling window. Availability unknown."* (`AVL-006`).
     - **Multi-Attribute Sorting Engine**: Sorts results deterministically by `relevance` (distance when coordinates provided, experience otherwise), `distance` (nearest first), `rating` (highest first), `experience` (most experienced first), `price_asc` (lowest full day rate), and `price_desc` (highest full day rate).
     - **Single Freelancer Card Retrieval**: Implemented `getFreelancerCardById(freelancerId, date, startTime, endTime)` providing full card metadata and live availability evaluation for individual candidate review.

3. **REST Endpoints**:
   - `GET /api/freelancers/search`: Query parameter-based multi-parameter freelancer search (DIS-001, DIS-003).
   - `POST /api/freelancers/search`: Request body-based search supporting complex programmatic queries.
   - `GET /api/freelancers/{id}/card`: Single freelancer discovery card with live availability inspection (DIS-004, DIS-005).

4. **React Frontend Studio Discovery Interface**:
   - `types/index.ts`: Added `FreelancerCard`, `FreelancerSearchFilter`, and `FreelancerSearchResponse`.
   - `services/freelancerService.ts`: Added `searchFreelancers(filters)` and `getFreelancerCard(id, date, startTime, endTime)`.
   - `StudioDiscoveryPage.tsx`: Premium, full-featured discovery interface with:
     - Top search panel with keyword search, shoot date picker, time slot selector, and quick city preset chips (Chennai, Coimbatore, Madurai, Bangalore, Kochi).
     - "My GPS" browser geolocation button for real-time Haversine distance from studio's current device position.
     - Advanced expandable filter drawer with services, skills, equipment, day-type switcher (`Full Day` vs `Half Day`), budget input, min experience dropdown, max distance radius, and sorting selector.
     - Rolling 10-day availability banner dynamically adapting to selected date (green for within-window verified available candidates; amber for beyond 10-day unknown availability notice).
     - Results view mode switcher (`Grid` vs `List`).
     - Creator Cards (DIS-004, DIS-005) with avatar, full name, primary role, star rating & review count, location & Haversine distance in km, full & half day pricing, skills & services badges, gear highlights, live availability badge (`Available 09:00 - 18:00` or `Date > 10 Days`), and AI match score badge (`94% Match`) (DIS-006).
     - "View Profile" interactive modal showcasing:
       - Portfolio showcase tab loading live categories and images from `/api/portfolio/freelancer/{id}`.
       - 10-day availability schedule tab loading live daily status from `/api/freelancers/{id}/availability`.
       - Equipment & skills breakdown tab with camera, lens, and lighting gear.
     - "Send Request" modal allowing studio to specify shoot details, dates, and message with instant submission confirmation.
   - `StudioDashboardPage.tsx`: Activated Card 1 ("Discover Freelancers") linking to `/studio/discovery`, added Phase 8 Active banner.
   - `Navbar.tsx`: Added "Find Creators" link with Search icon in Studio navigation, updated platform badge to "Phase 8 Active".
   - `App.tsx`: Registered protected route `/studio/discovery` for `STUDIO` and `ADMIN` roles.

### Verification Summary
- **Backend Test Suite**: `mvn test` -> **40 of 40 tests PASSED (0 failures, 0 errors, 0 skipped)** in 34.9s:
  1. `StudioLynkApplicationTests` (5 tests)
  2. `AuthControllerTests` (9 tests)
  3. `StudioControllerTests` (4 tests)
  4. `FreelancerControllerTests` (5 tests)
  5. `PortfolioControllerTests` (5 tests)
  6. `FreelancerAvailabilityControllerTests` (6 tests)
  7. `FreelancerDiscoveryControllerTests` (6 tests):
     - `testBasicDiscoverySearch` (DIS-001, DIS-004, DIS-005)
     - `testMultiParameterFilters` (DIS-003: keyword, service, skill, minExperience, maxBudget)
     - `testHaversineDistanceAndSorting` (DIS-003: Haversine distance calculation, max distance radius, distance sorting)
     - `testAvailabilityFilteringInDiscovery` (AVL-004, AVL-005, AVL-006, DIS-003: 10-day window inclusion/exclusion & beyond window notice)
     - `testGetFreelancerCardById` (DIS-004, DIS-005: individual card with live availability evaluation)
     - `testIncompleteOnboardingExcluded` (DIS-001: onboarding gate enforcement)
- **Frontend Build**: `npm run build` -> **Compiled cleanly with TypeScript type-checking and Vite production asset bundling in 6.56s (0 errors)**.

---

## Phase 9 — Work Requirements & Shoot Specifications
**Completed Date:** 2026-09-28  
**Status:** COMPLETED & TESTED

### Implemented Modules & Capabilities
1. **JPA Domain Entities & Database Mappings**:
   - `WorkRequirement` (`work_requirements` table): Extends `BaseEntity` with auditing timestamps, `@ManyToOne` to `Studio`, `@ManyToOne` to `Freelancer` (for confirmed creator), and `@ManyToMany` mappings:
     - `skills` mapped via MySQL join table `work_requirement_skills` (`work_requirement_id`, `skill_id`).
     - `services` mapped via MySQL join table `work_requirement_services` (`work_requirement_id`, `service_id`).
     - `equipment` mapped via MySQL join table `work_requirement_equipment` (`work_requirement_id`, `equipment_id`).
   - Fields: `eventName`, `eventType`, `eventDate`, `startTime`, `endTime`, `location`, `latitude`, `longitude`, `dayType` (`FULL_DAY`, `HALF_DAY`), `budget`, `description`, `status` (`RequirementStatus`), protected `eventContactName`, and protected `eventContactPhone`.
   - Created `WorkRequirementRepository` with queries for studio requirements, open requirements, and count by studio and status.

2. **Data Transfer Objects (DTOs)**:
   - `WorkRequirementRequestDto`: Validates event name, event type, shoot date, start/end times with strict chronological ordering (`startTime.isBefore(endTime)`), venue location, budget non-negativity, criteria selection ID lists, and private client contact info.
   - `WorkRequirementResponseDto`: Full requirement presentation model featuring privacy control flags (`hasPrivateContactDetails`, `privateDetailsRevealed`), formatted times, resolved skills, services, gear lists, and studio details.
   - `WorkRequirementSummaryDto`: Lightweight summary DTO for listings, cards, and dashboards with aggregate criteria counts (`skillsCount`, `servicesCount`, `equipmentCount`).
   - `RequirementStatusUpdateDto`: Validated status transition payload.

3. **Service Layer Architecture & Business Logic (WRK-001 - WRK-008, REQ-002, REQ-006)**:
   - `WorkRequirementService` and `WorkRequirementServiceImpl`:
     - **Requirement Creation (WRK-001, WRK-002)**: Studio-restricted endpoint resolving `Studio` from authenticated user context, persisting skill/service/equipment associations, validating chronological shoot times, and initializing status (`OPEN` or `DRAFT`).
     - **Requirement Updating**: Ownership-guarded update capability allowing studio to modify event specifications, criteria checklists, and contact info before confirmation.
     - **Strict Client Contact Privacy Protection (REQ-002, REQ-006)**:
       - If caller is the owning studio: reveals full client contact details (`eventContactName`, `eventContactPhone`, `privateDetailsRevealed = true`).
       - If caller is the confirmed freelancer: reveals client contact details for shoot day coordination.
       - If caller is an unconfirmed creator or public viewer: strictly masks contact info (`eventContactName = null`, `eventContactPhone = null`, `privateDetailsRevealed = false`), while `hasPrivateContactDetails` indicates whether private info is on file.
     - **Requirement Lifecycle Management (WRK-003)**: Validated state transitions across `DRAFT`, `OPEN`, `REQUESTED`, `ACCEPTED`, `CONFIRMED`, `IN_PROGRESS`, `COMPLETED`, and `CANCELLED`.
     - **Discovery & AI Matching Pre-Population Link (WRK-005)**: Direct parameterization linking requirement dates, times, day type, and budget into `/studio/discovery`.
     - **Single Confirmed Freelancer Architecture Foundation (WRK-006, WRK-007, WRK-008)**: Database and service structure ready for work request generation and single confirmed assignment.
     - **Open Requirements Public Feed (WRK-004)**: Filtered feed for open shoots available for general viewing.
     - **Requirement Deletion**: Secure deletion guarded by ownership and non-active status checks.

4. **Security & Exception Handling**:
   - `SecurityConfig`: Permitted public access to `GET /api/requirements/open`.
   - `GlobalExceptionHandler`: Added explicit handler for `AccessDeniedException` mapping to HTTP 403 Forbidden.

5. **REST Endpoints**:
   - `POST /api/requirements`: Create work requirement (WRK-001, WRK-002).
   - `GET /api/requirements/{id}`: Get requirement details with dynamic privacy filtering (REQ-002, REQ-006).
   - `PUT /api/requirements/{id}`: Update requirement details.
   - `DELETE /api/requirements/{id}`: Delete requirement.
   - `GET /api/requirements/studio/me`: List all requirements for authenticated studio.
   - `PATCH /api/requirements/{id}/status`: Update lifecycle status (WRK-003).
   - `GET /api/requirements/open`: Public/freelancer open requirements feed (WRK-004).

6. **React Frontend Studio Requirements Workspace**:
   - `types/index.ts`: TypeScript interfaces for `RequirementStatus`, `WorkRequirement`, `WorkRequirementSummary`, and `WorkRequirementPayload`.
   - `services/requirementService.ts`: Reusable API client for all requirement CRUD, status transitions, and queries.
   - `StudioRequirementsPage.tsx`: Complete requirements management interface featuring:
     - Header banner with Phase 9 badge and "Post New Requirement" button.
     - 4 real-time metrics cards: Total Requirements, Open Shoots, Confirmed/Active, and Committed Budget.
     - Status tabs filter (`All`, `Open`, `Drafts`, `Confirmed`, `Completed`, `Cancelled`) and real-time text search.
     - Requirement cards with status badges, event specifications, criteria counts (skills, services, equipment), budget, direct AI Discovery creator link (WRK-005), and details navigation.
   - `StudioCreateRequirementPage.tsx`: Comprehensive multi-section creation and editing wizard:
     - Shoot fundamentals: event title, category, shoot date, call time, wrap time, coverage type (`Full Day` vs `Half Day`), venue address, budget in INR, creative brief.
     - Criteria checklists: interactive skill chips, service chips, equipment gear chips with custom item addition.
     - Confidential Client Contact section (REQ-002, REQ-006) with privacy security guarantee banner.
     - "Save as Draft" and "Publish Shoot Requirement" submission actions.
   - `StudioRequirementDetailPage.tsx`: Rich requirement details workspace featuring:
     - Status timeline badge and lifecycle transition buttons (Publish, Revert to Draft, Mark Complete, Cancel).
     - Event specification grid (Date, Schedule & Coverage, Budget, Venue).
     - Technical specifications breakdown (Required Skills, Services & Deliverables, Gear Checklist).
     - Confidential client contact card with Gated REQ-002 indicator and direct phone link.
     - Prominent "Find Matching Creators (AI Discovery)" action pre-populating discovery query parameters (WRK-005).
     - Delete confirmation modal.
   - `StudioDashboardPage.tsx`: Activated Card 2 ("Work Requirements") linking to `/studio/requirements`, added Recent Work Requirements list, and Phase 9 Active banner.
   - `Navbar.tsx`: Added "Requirements" navigation link with Briefcase icon for Studios, updated badge to "Phase 9 Active".
   - `App.tsx`: Registered protected routes for `/studio/requirements`, `/studio/requirements/new`, `/studio/requirements/:id`, and `/studio/requirements/:id/edit`.

### Verification Summary
- **Backend Test Suite**: `mvn test` -> **47 of 47 tests PASSED (0 failures, 0 errors, 0 skipped)** in 38.7s:
  1. `StudioLynkApplicationTests` (5 tests)
  2. `AuthControllerTests` (9 tests)
  3. `StudioControllerTests` (4 tests)
  4. `FreelancerControllerTests` (5 tests)
  5. `PortfolioControllerTests` (5 tests)
  6. `FreelancerAvailabilityControllerTests` (6 tests)
  7. `FreelancerDiscoveryControllerTests` (6 tests)
  8. `WorkRequirementControllerTests` (7 tests):
     - `testCreateWorkRequirementSuccess`: WRK-001, WRK-002 creation and field persistence.
     - `testPrivateContactInfoMaskedForUnconfirmedUser`: REQ-002 privacy gating masks contact name and phone from unconfirmed callers.
     - `testOwnerCanSeePrivateContactInfo`: REQ-002, REQ-006 studio owner sees full client contact details.
     - `testUpdateRequirementStatusLifecycle`: WRK-003 status transitions (`DRAFT` -> `OPEN` -> `CANCELLED`).
     - `testUpdateAndGetStudioRequirements`: Edit requirement details and query studio requirements list.
     - `testInvertedTimeThrowsValidationError`: WRK-002 start time after end time fails validation.
     - `testFreelancerCannotCreateRequirement`: Role-based security check (403 Forbidden for freelancers).
- **Frontend Build**: `npm run build` -> **Compiled cleanly with TypeScript type-checking and Vite production asset bundling in 8.91s (0 errors)**.

---

## Phase 10 — Work Requests, Candidate Bidding & Confirmation (REQ-001 - REQ-009, WRK-006 - WRK-008)
**Completed Date:** 2026-09-28  
**Status:** COMPLETED & TESTED

### Implemented Modules & Capabilities
1. **Database Schema & Flyway Migration Engine**:
   - Created `backend/src/main/resources/db/migration/V2__add_work_request_message.sql`:
     - Added `message TEXT NULL` column to `work_requests` table to support personalized shoot briefs and negotiation notes.
     - Preserved all foreign keys, status enums (`PENDING`, `ACCEPTED`, `REJECTED`, `CONFIRMED`, `CANCELLED`), and audit columns.
     - Hibernate ORM `ddl-auto: validate` passed with complete entity-schema synchronization.

2. **JPA Entity Model & Persistence Layer**:
   - `WorkRequest.java`: Mapped to `work_requests` table extending `BaseEntity` (inheriting automated `createdAt` and `updatedAt` timestamps).
     - ManyToOne relationships to `WorkRequirement` and `Freelancer`.
     - Fields: `status` (`RequestStatus`), `agreedPrice` (`BigDecimal`), `message` (`String`), `cancellationReason` (`String`).
   - `WorkRequestRepository.java`:
     - `findByRequirementId`: Retrieve all work requests / candidates dispatched for a requirement.
     - `findByFreelancerId`: Retrieve all work requests received by a freelancer.
     - `findByFreelancerIdAndStatus`: Query by status (e.g. pending requests).
     - `existsByRequirementIdAndFreelancerId`: Prevent duplicate dispatching of work requests to the same creator for a requirement.
     - `findByRequirementIdAndStatus`: Query candidates in a given status (e.g. pending/accepted).
     - `findByFreelancerIdAndStatusAndEventDate`: Interval conflict detection for double-booking checks.

3. **Data Transfer Objects (DTOs)**:
   - `CreateWorkRequestDto.java`: Validates requirement ID, freelancer ID, optional offered price, and optional message.
   - `AcceptRequestDto.java`: Validates proposed/accepted price during freelancer acceptance.
   - `CancelRequestDto.java`: Strictly validates mandatory cancellation reason (REQ-007).
   - `WorkRequestResponseDto.java`: Full work request response including shoot details, required skills/services/equipment, gated client contact flags, and privacy masking.
   - `WorkRequestSummaryDto.java`: Compact card DTO for list feeds with status, price, freelancer details, studio info, and cancellation reason.

4. **Service Layer & Business Rules (WorkRequestServiceImpl)**:
   - **REQ-001 & WRK-006 (Send Work Request & Multiple Candidates)**: Studio can send work requests to one or more freelancers for an open requirement. Validates requirement ownership, status (`OPEN` or `REQUESTED`), and ensures no duplicate requests.
   - **REQ-002 & REQ-006 (Gated Client Privacy Architecture)**: When a freelancer views a work request or requirement before confirmation, confidential client contact details (`eventContactName`, `eventContactPhone`) are dynamically stripped (`null`) and flagged as unrevealed. They are revealed *only* when the work request status is `CONFIRMED`.
   - **REQ-004 (Freelancer Accept / Reject)**:
     - Freelancer can accept a pending request and specify/confirm the agreed price (REQ-008). Requirement status transitions to `ACCEPTED`.
     - Freelancer can reject a request with an optional reason.
   - **REQ-005 & WRK-007 (Studio Confirmation)**: Studio confirms one candidate creator for the assignment. Work request status transitions to `CONFIRMED`, and requirement status transitions to `CONFIRMED`.
   - **WRK-008 (Automatic Closure of Competing Candidates)**: Upon studio confirmation of one creator, all other candidate requests for that requirement with status `PENDING` or `ACCEPTED` are automatically transitioned to `REJECTED` with the cancellation note: *"Studio confirmed another creator for this assignment."*
   - **REQ-008 (Agreed Price Persistence)**: Stores the final agreed price on the work request record upon acceptance and confirmation.
   - **REQ-009 (Double Booking Prevention)**: Implemented `checkDoubleBooking(...)` checking for existing `CONFIRMED` assignments for the freelancer on the same date with overlapping time intervals (`!(startTime.isAfter(reqEnd) || endTime.isBefore(reqStart))`). Throws `BadRequestException` if double booking is attempted.
   - **REQ-007 (Cancellation with Mandatory Reason)**: Both studio and freelancer can cancel a confirmed booking, provided a non-blank cancellation reason is supplied. Requirement reverts to `OPEN`.

5. **REST API Controller (WorkRequestController)**:
   - `POST /api/requests`: Send a work request (Studio only).
   - `GET /api/requests/{id}`: Get work request details with dynamic privacy gating.
   - `PATCH /api/requests/{id}/accept`: Accept request with agreed price (Freelancer only).
   - `PATCH /api/requests/{id}/reject`: Reject request (Freelancer only).
   - `PATCH /api/requests/{id}/confirm`: Confirm creator booking and auto-close other candidates (Studio only).
   - `PATCH /api/requests/{id}/cancel`: Cancel confirmed work with mandatory reason (Studio or Freelancer).
   - `GET /api/requests/freelancer/me`: List all requests received by authenticated freelancer.
   - `GET /api/requests/studio/requirement/{requirementId}`: List all candidate requests for a requirement (Studio only).
   - `GET /api/requests/studio/me`: List all work requests sent by authenticated studio.

6. **React Frontend Work Requests Workspace**:
   - `types/index.ts`: TypeScript models for `RequestStatus`, `WorkRequest`, `WorkRequestSummary`, and `CreateWorkRequestPayload`.
   - `services/requestService.ts`: Complete API client with methods for sending, fetching, accepting, rejecting, confirming, and cancelling requests.
   - `FreelancerRequestsPage.tsx`:
     - Inbox of all incoming studio requests with status filter tabs (`All`, `Pending`, `Accepted`, `Confirmed`, `Rejected`, `Cancelled`).
     - Cards showing event details, studio name, shoot schedule, offered price, and quick actions (Review Shoot Details).
   - `FreelancerRequestDetailPage.tsx`:
     - Complete shoot specification view with event details, schedule, venue, required skills, services, and gear checklist.
     - **REQ-002 / REQ-006 Privacy Card**: Confidential client contact information remains locked with a shield banner when unconfirmed; cleanly unlocks with client name and direct phone link upon confirmation.
     - Action bar: "Accept Assignment" modal with rate confirmation, "Decline Proposal" modal with reason, "Message Studio" link (REQ-003), and "Cancel Booking" action if confirmed.
   - `StudioRequestsPage.tsx`:
     - Studio sent requests feed grouped by requirement.
     - Displays candidate creators, offered/agreed rates, and quick "Confirm Creator" action for accepted candidates.
   - `StudioRequirementDetailPage.tsx`:
     - Added Candidate Creators & Proposals section displaying all applicants.
     - Added "Confirm Creator" action (WRK-007) and "Cancel Assignment" modal (REQ-007).
   - `StudioDiscoveryPage.tsx`:
     - Wired "Send Request" modal to load studio's active open requirements, select a requirement, enter an offered price and message, and dispatch via `requestService.createRequest(...)`.
   - `FreelancerDashboardPage.tsx`:
     - Activated Card 4 ("Incoming Work Requests") linking to `/freelancer/requests` with Phase 10 Active badge.
   - `Navbar.tsx`:
     - Added "Requests" navigation link for Studios (`/studio/requests`) and Freelancers (`/freelancer/requests`), updated badge to "Phase 10 Active".
   - `App.tsx`:
     - Registered protected routes for `/studio/requests`, `/freelancer/requests`, and `/freelancer/requests/:id`.

### Verification Summary
- **Backend Test Suite**: `mvn test` -> **55 of 55 tests PASSED (0 failures, 0 errors, 0 skipped)** in 51.5s:
  1. `StudioLynkApplicationTests` (5 tests)
  2. `AuthControllerTests` (9 tests)
  3. `StudioControllerTests` (4 tests)
  4. `FreelancerControllerTests` (5 tests)
  5. `PortfolioControllerTests` (5 tests)
  6. `FreelancerAvailabilityControllerTests` (6 tests)
  7. `FreelancerDiscoveryControllerTests` (6 tests)
  8. `WorkRequirementControllerTests` (7 tests)
  9. `WorkRequestControllerTests` (8 tests):
     - `testSendWorkRequestSuccess`: REQ-001, WRK-006 dispatching work requests.
     - `testFreelancerInitiallyCannotSeePrivateContactInfo`: REQ-002 client name & phone masked before confirmation.
     - `testFreelancerAcceptAndStudioConfirmFlow`: REQ-004, REQ-005, REQ-006, REQ-008 acceptance, rate negotiation, studio confirmation, and client contact reveal.
     - `testAutoRejectOtherCandidatesOnConfirmation`: WRK-008 auto-closing competing candidates upon confirmation.
     - `testPreventDoubleBookingOnOverlappingInterval`: REQ-009 double booking prevention rejecting overlapping confirmed assignments.
     - `testCancelConfirmedWorkWithMandatoryReason`: REQ-007 cancellation with mandatory reason.
     - `testCancelWithoutReasonFails`: REQ-007 blank reason rejection.
     - `testFreelancerRejectRequest`: REQ-004 freelancer decline proposal.
- **Frontend Build**: `npm run build` -> **Compiled cleanly with TypeScript type-checking and Vite production asset bundling in 7.18s (0 errors)**.

---

## Phase 11 — WebSocket Messaging & Real-Time Negotiation (MSG-001 – MSG-006)
**Completed Date:** 2026-09-28  
**Status:** COMPLETED & TESTED

### Implemented Modules & Capabilities
1. **Spring WebSocket & STOMP Protocol Architecture (MSG-002)**:
   - Added `spring-boot-starter-websocket` to `backend/pom.xml`.
   - Permitted `/ws/**` endpoint in `SecurityConfig.java` for HTTP WebSocket upgrade handshakes.
   - Implemented `WebSocketConfig` configuring:
     - STOMP endpoints at `/ws` with pure WebSocket and SockJS fallback support.
     - Application destination prefix `/app` (routes to `@MessageMapping` handlers).
     - User destination prefix `/user` and simple in-memory broker prefixes `/topic` and `/queue`.
     - Inbound channel interceptor: decodes and validates JWT bearer tokens extracted from the STOMP `CONNECT` frame (`Authorization` or `token` header) using `JwtTokenProvider.validateToken(...)`, setting authenticated `UsernamePasswordAuthenticationToken` on the WebSocket session accessor.
   - Configured `vite.config.ts` proxy with `ws: true` for development WebSocket proxying to Spring Boot on port 8080.

2. **JPA Data Models & Persistence (MSG-001, MSG-003, MSG-004)**:
   - `Conversation` entity mapped to `conversations` table:
     - Relationships to `WorkRequirement`, `Studio`, and `Freelancer`.
     - Enforces exactly one conversation thread per (requirement, freelancer) pair (`uq_conv_req_fl`).
     - Includes auditing timestamp `createdAt`.
   - `Message` entity mapped to `messages` table:
     - Many-to-one relationship to `Conversation` and `User` (sender).
     - Pure text-only content validated up to 4000 characters (MSG-003).
     - Sent timestamp `sentAt` (`LocalDateTime`) and read status `isRead` boolean flag (MSG-004).
   - `ConversationRepository` & `MessageRepository`:
     - Query conversation by requirement & freelancer.
     - Find all conversations involving a studio or freelancer with latest message ordering.
     - Bulk patch method: `markMessagesAsRead(conversationId, readerId)`.
     - Fast count query: `countUnreadMessages(conversationId, readerId)`.

3. **Core Messaging Service Layer (ConversationService & ConversationServiceImpl)**:
   - **Requirement Authorization & Membership Gate (MSG-001)**:
     - Studios can only participate in conversations attached to requirements they own.
     - Freelancers can only participate in conversations if they hold a valid work request for that requirement.
     - Non-participants are strictly blocked with `UnauthorizedException` or `ResourceNotFoundException`.
   - **Privacy Protection Gate (REQ-002, REQ-006)**:
     - Message content remains strictly private to the two participants (Studio & Creator).
     - Confidential client details are never injected into chat before confirmation.
   - **Real-Time Dispatching (MSG-002)**:
     - Persists messages via `messageRepository.save(...)`.
     - Dispatches real-time STOMP payload via `SimpMessagingTemplate` to destination `/topic/conversation.{conversationId}`.
   - **Read Receipt & Unread Counter Tracking (MSG-004)**:
     - `markAsRead(conversationId, user)` atomically updates unread messages sent by the opposing counterparty.

4. **REST & STOMP Controllers**:
   - `ConversationController` (`/api/conversations`):
     - `GET /api/conversations`: Returns all conversations for authenticated user with unread counts and last message previews.
     - `GET /api/conversations/{id}`: Returns conversation details.
     - `GET /api/conversations/requirement/{requirementId}`: Get or create conversation thread for requirement.
     - `GET /api/conversations/{id}/messages`: Fetch chronological message history (MSG-004).
     - `POST /api/conversations/{id}/messages`: REST fallback / message dispatch (MSG-003, MSG-004).
     - `PATCH /api/conversations/{id}/read`: Mark all messages in conversation as read (MSG-004).
   - `WsChatController`:
     - `@MessageMapping("/chat.sendMessage")`: Handles incoming STOMP messages, authenticates sender, persists, and publishes to `/topic/conversation.{id}`.

5. **React Frontend Messaging Experience**:
   - Installed `@stomp/stompjs` (v7) for high-performance WebSocket/STOMP client connection management.
   - `messagingService.ts`: REST methods for conversation fetching and STOMP client factory with auto-reconnection and heartbeat handling.
   - `MessagesPage.tsx`:
     - Dual-pane layout: conversation directory sidebar with search and unread badges, and active chat window with message history.
     - Live connection status indicator (Connected / Reconnecting) with visual pulse dot.
     - Read status indicators (single check for sent, double check for read).
     - Quick negotiation prompt chips for rapid communication (Confirm call time, Negotiate rate, Gear checklist, Travel details) (MSG-005).
     - Character counter warning (up to 4000 chars) and enter-to-send keyboard shortcuts.
   - Contextual routing:
     - `FreelancerRequestDetailPage.tsx`: "Message Studio" button in action bar and Studio card.
     - `FreelancerRequestsPage.tsx`: "Chat" button on every incoming request card.
     - `StudioRequirementDetailPage.tsx`: "Chat" action for every candidate creator proposal.
     - `StudioRequestsPage.tsx`: "Chat" action for every dispatched request card.
     - `StudioDashboardPage.tsx`: Activated Card 3 ("Direct Messaging") with Phase 11 Active badge.
     - `FreelancerDashboardPage.tsx`: Added Card 5 ("Live Studio Chat & Rate Negotiation") with Phase 11 Active badge.
     - `Navbar.tsx`: Added "Messages" navigation link with `MessageSquare` icon for Studios and Freelancers, updated badge to "Phase 11 Active".
     - `App.tsx`: Registered `/messages` protected route for `['STUDIO', 'FREELANCER', 'ADMIN']`.

### Verification Summary
- **Backend Test Suite**: `mvn test` -> **63 of 63 tests PASSED (0 failures, 0 errors, 0 skipped)** in 47.6s:
  - 8 new dedicated Phase 11 integration tests in `ConversationControllerTests`:
    1. `testCreateAndGetConversationForRequirement`: MSG-001, MSG-005 get-or-create conversation for requirement.
    2. `testSendMessageSuccess`: MSG-003, MSG-004 message creation, timestamping, unread flag.
    3. `testStudioCannotMessageUninvitedFreelancer`: MSG-001 requirement authorization gating.
    4. `testMessageContentValidation`: MSG-003 blank message rejection.
    5. `testMessageLengthExceededFails`: MSG-003 4000+ character limit enforcement.
    6. `testMarkMessagesAsRead`: MSG-004 bulk read patch endpoint.
    7. `testGetUserConversations`: Conversation feed retrieval with unread counts.
    8. `testConfidentialClientDetailsMaskedDuringNegotiation`: REQ-002, MSG-005 client privacy preservation.
- **Frontend Build**: `npm run build` -> **Compiled cleanly with TypeScript type-checking and Vite production asset bundling in 7.46s (0 errors)**.

---

## Phase 12 — In-App Notifications & Activity Center
**Completed Date:** 2026-09-28  
**Status:** COMPLETED & TESTED

### Implemented Modules & Capabilities
1. **In-App Notification Engine & JPA Architecture (NOT-001, NOT-003)**:
   - Database schema: leveraged existing normalized `notifications` table from Flyway baseline schema (`id`, `user_id`, `type`, `title`, `message`, `is_read`, `related_entity_type`, `related_entity_id`, `created_at`).
   - JPA entity: `Notification.java` with `@ManyToOne` association to `User`, automatic timestamp auditing, and `@Enumerated(EnumType.STRING)` mapping.
   - Spring Data JPA repository: `NotificationRepository.java` featuring indexed user inbox queries (`findByUserIdOrderByCreatedAtDescIdDesc`, `findByUserIdAndIsReadFalseOrderByCreatedAtDescIdDesc`), unread counts (`countByUserIdAndIsReadFalse`), bulk mark all read (`markAllAsReadByUserId`), and atomic mark single read (`markAsReadByIdAndUserId`).
   - DTO abstraction: `NotificationDto.java` with explicit `@JsonProperty("isRead")` on fields/getters to ensure consistent JSON serialization across clients, and dynamic computed `linkUrl` mapping to frontend detail pages based on notification type and related entities.

2. **Full Lifecycle Notification Event Triggers (NOT-002)**:
   - **New Work Request (`REQUEST_RECEIVED`)**: Dispatched to creator upon direct studio work request.
   - **Request Accepted (`REQUEST_ACCEPTED`)**: Dispatched to studio owner when creator accepts.
   - **Request Rejected (`REQUEST_REJECTED`)**: Dispatched to studio owner when creator declines.
   - **Studio Confirmation (`STUDIO_CONFIRMATION`)**: Dispatched to creator when studio confirms booking and unlocks client details.
   - **New Chat Message (`NEW_MESSAGE`)**: Dispatched to the counterparty in requirement negotiation threads.
   - **Work Started (`WORK_STARTED`)**: Dispatched to assigned creator when requirement transitions to `IN_PROGRESS`.
   - **Work Completed (`WORK_COMPLETED`)**: Dispatched to creator when requirement status moves to `COMPLETED`.
   - **Work Cancelled (`WORK_CANCELLED`)**: Dispatched to the affected counterparty when a confirmed booking or requirement is cancelled.
   - **Rating Reminder (`RATING_REMINDER`)**: Dispatched to both studio and creator upon work completion to prompt mutual reviews.

3. **Real-Time Push & Read/Unread State Management (NOT-003, NOT-004)**:
   - WebSocket/STOMP template integration: `SimpMessagingTemplate` broadcasts JSON payload directly to `/topic/notifications.{userId}` on every notification trigger.
   - Zero external email dependency (NOT-001): in-app only, protecting user privacy and eliminating SMTP spam/delivery friction.
   - Individual read marking: `PATCH /api/notifications/{id}/read` updates state and returns updated notification.
   - Bulk mark-all-as-read: `PATCH /api/notifications/read-all` clears all unread badges across the user inbox.
   - Unread counter endpoint: `GET /api/notifications/unread-count` returns live unread count.

4. **React Frontend Notification Center & Navbar Integration**:
   - `types/index.ts`: Added `NotificationType`, `AppNotification`, and `UnreadNotificationCount` TypeScript contracts.
   - `notificationService.ts`: REST client for list, unread count, read single, and read all endpoints.
   - `NotificationsPage.tsx` (`/notifications`):
     - Filter tabs: All Notifications vs Unread Only.
     - Contextual icon badges and color coding matching event type (e.g. green for confirmed/accepted, rose for rejected/cancelled, violet for messages, amber for rating reminders).
     - Direct jump navigation to related requirements, requests, or chats via `linkUrl`.
     - Single-click "Mark as read" and bulk "Mark all read" controls.
     - Relative timestamps ("just now", "10m ago", "2h ago", "1d ago") and full datetime tooltips.
   - `Navbar.tsx`:
     - Interactive Bell icon button with red badge counter for unread notifications.
     - Flyout dropdown displaying the 5 most recent notifications with unread indicators, mark-all-read shortcut, and direct link to the full notifications center.
     - Polling and route-change refresh keeping badge counts in sync.
     - Updated header badge to "Phase 12 Active".
   - `App.tsx`: Protected route registration for `/notifications`.

### Verification Summary
- **Backend Test Suite**: `mvn test` -> **72 of 72 tests PASSED (0 failures, 0 errors, 0 skipped)** in 53.4s:
  - 9 new dedicated Phase 12 integration tests in `NotificationControllerTests`:
    1. `testGetNotificationsRequiresAuth`: Unauthenticated requests rejected with 401.
    2. `testEmptyNotificationsListForNewUser`: Clean inbox retrieval with zero errors.
    3. `testNotificationCreatedOnWorkRequest`: NOT-002 `REQUEST_RECEIVED` notification created and delivered to creator.
    4. `testUnreadNotificationCount`: NOT-003 unread count retrieval.
    5. `testMarkSingleNotificationAsRead`: NOT-004 individual notification read patch.
    6. `testMarkAllNotificationsAsRead`: NOT-004 bulk mark-all-as-read endpoint.
    7. `testCannotMarkOtherUserNotifications`: Security authorization gate preventing cross-user mutation.
    8. `testNotificationCreatedOnNewMessage`: NOT-002 `NEW_MESSAGE` notification dispatched to counterparty.
    9. `testNotificationOnRequirementStatusChange`: NOT-002 `WORK_STARTED`, `WORK_COMPLETED`, and `RATING_REMINDER` notifications dispatched on lifecycle transitions.
- **Frontend Build**: `npm run build` -> **Compiled cleanly with TypeScript type-checking and Vite production asset bundling in 7.54s (0 errors)**.

---

## Phase 13 — Rating System & Mutual Profile Reviews
**Completed Date:** 2026-09-29  
**Status:** COMPLETED & TESTED

### Implemented Modules & Capabilities
1. **JPA Entity & Repository Architecture (RAT-001, RAT-002, RAT-005, RAT-006)**:
   - Database schema: leveraged existing normalized `ratings` table from Flyway baseline schema (`id`, `requirement_id`, `from_user_id`, `to_user_id`, `target_type`, `score`, `review_text`, `created_at`, `uq_rating_req_user`).
   - JPA entity: `Rating.java` with `@ManyToOne` associations to `WorkRequirement`, `fromUser`, `toUser`, `@Enumerated(EnumType.STRING)` mapping to `RatingTargetType`, `@CreationTimestamp` auditing, and unique constraint enforcement on `(requirement_id, from_user_id)`.
   - Spring Data JPA repository: `RatingRepository.java` featuring indexed query methods:
     - `existsByRequirementIdAndFromUserId`
     - `findByRequirementIdAndFromUserId`
     - `findByToUserIdAndTargetTypeOrderByCreatedAtDesc`
     - Aggregate JPQL queries calculating `AVG(r.score)` and `COUNT(r)` for profile summaries.
   - DTO abstraction:
     - `RatingSubmissionDto.java` enforcing Bean Validation (`@NotNull`, `@Min(1)`, `@Max(5)`, `@Size(max = 2000)`).
     - `RatingDto.java` containing full review metadata and participant display names.
     - `RatingSummaryDto.java` providing computed average rating, total count, and reviews list.
     - `RequirementRatingStatusDto.java` supplying requirement eligibility, user's rating, and counterparty rating.

2. **Core Rating Service & Business Rule Verification**:
   - **Post-Completion Lifecycle Gating (12-RATING-SPECIFICATION.md)**: Requirements must reach `COMPLETED` status before any rating can be submitted. Attempts on uncompleted work are rejected with `400 Bad Request`.
   - **Two-Way Mutual Rating (RAT-001, RAT-002)**:
     - Studios can rate confirmed freelancers (`targetType = FREELANCER`).
     - Freelancers can rate owning studios (`targetType = STUDIO`).
   - **Participant Authorization Gating**: Unrelated third parties attempting to rate a requirement are blocked with `403 Forbidden`.
   - **One Rating Per Direction (RAT-006)**: Duplicate ratings in the same direction on a completed requirement are blocked with `409 Conflict` (`DuplicateResourceException`).
   - **Real-Time In-App Notification Trigger**: Dispatching `NotificationType.RATING_REMINDER` to counterparty upon rating submission with star score and author details.
   - **ML Ranking Feature Separation (RAT-004, 18-DECISIONS-AND-CONSTRAINTS.md)**: Ratings are strictly isolated from AI recommendation ranking features.

3. **REST Controller Endpoints**:
   - `POST /api/requirements/{id}/ratings`: Authenticated submission of 1-5 star score and review text.
   - `GET /api/requirements/{id}/ratings/status`: Authenticated check for requirement rating state.
   - `GET /api/freelancers/{id}/ratings`: Public endpoint returning average rating, count, and verified reviews (RAT-003).
   - `GET /api/studios/{id}/ratings`: Public endpoint returning average rating, count, and verified reviews (RAT-003).
   - Spring Security: Updated `SecurityConfig.java` to whitelist public rating endpoints.

4. **React Frontend Rating Experience & Integration**:
   - `types/index.ts`: Added `RatingDto`, `RatingSubmissionDto`, `RatingSummaryDto`, and `RequirementRatingStatusDto`.
   - `ratingService.ts`: REST client for ratings endpoints.
   - `RequirementRatingSection.tsx`: Reusable interactive rating component with 1-5 star hover selector, custom star labels, optional review input, character counter, and cards displaying user's submitted rating and counterparty feedback.
   - `PublicReviewsList.tsx`: Reusable public reviews feed displaying verified rating badge, average score, total reviews, and formatted review cards.
   - `StudioRequirementDetailPage.tsx`: Integrated `RequirementRatingSection` for completed shoots.
   - `FreelancerRequestDetailPage.tsx`: Integrated `RequirementRatingSection` for completed shoots.
   - `FreelancerProfilePage.tsx`: Integrated `PublicReviewsList` for verified client feedback.
   - `StudioProfilePage.tsx`: Integrated `PublicReviewsList` for verified creator feedback.
   - `StudioDiscoveryPage.tsx`: Added "Reviews & Ratings" tab to candidate preview modal with `PublicReviewsList`.
   - `Navbar.tsx`: Updated badge to **"Phase 13 Active"**.

### Verification Summary
- **Backend Test Suite**: `mvn test` -> **81 of 81 tests PASSED (0 failures, 0 errors, 0 skipped)** in 58.3s:
  - 9 new dedicated Phase 13 integration tests in `RatingControllerTests`:
    1. `testCannotRateBeforeWorkIsCompleted`: Lifecycle requirement validation (400 Bad Request).
    2. `testStudioRatesFreelancerSuccess`: RAT-001 studio rating freelancer with score & review text.
    3. `testFreelancerRatesStudioSuccess`: RAT-002 freelancer rating studio with score & review text.
    4. `testCannotRateTwiceInSameDirection`: RAT-006 duplicate submission prevention (409 Conflict).
    5. `testBothDirectionsPermittedForCompletedWork`: Mutual two-way rating verification.
    6. `testUnrelatedUserCannotRateRequirement`: Security authorization gate (403 Forbidden).
    7. `testRatingScoreValidation`: Bean Validation rejection of scores < 1 and > 5.
    8. `testGetRequirementRatingStatus`: Eligibility and submitted status retrieval.
    9. `testPublicRatingsEndpointsWithoutAuth`: RAT-003 public unauthenticated endpoint accessibility.
- **Frontend Build**: `npm run build` -> **Compiled cleanly with TypeScript type-checking and Vite production asset bundling in 8.21s (0 errors)**.

---

## Phase 14 — Machine Learning Service & Match Scoring Model (ML-001 – ML-011)
**Completed Date:** 2026-09-29  
**Status:** COMPLETED & TESTED

### Implemented Modules & Capabilities
1. **Machine Learning Architecture & Technology Stack (ML-001, ML-002, 18-DECISIONS-AND-CONSTRAINTS.md)**:
   - Dedicated Python microservice running Python 3.12, FastAPI 0.115, pandas 2.2.3, NumPy 2.1.2, and scikit-learn 1.5.2.
   - Core model strictly implemented using `sklearn.tree.DecisionTreeRegressor` (substitutions such as Random Forest or LLM are strictly rejected per specification).

2. **Synthetic Dataset Generation with Documented Business Logic (ML-003, ML-004, ML-005, ML-006)**:
   - Script: `ml-service/app/dataset.py`.
   - Generated exactly 5,000 synthetic records (`N = 5000`) with deterministic random state (`random_state=42`).
   - Exactly 6 normalized features ($[0.0, 1.0]$):
     1. `skill_match` (0.30 weight — primary technical prerequisite)
     2. `portfolio_relevance` (0.25 weight — visual style & category alignment)
     3. `experience` (0.05 weight — craft maturity)
     4. `budget_compatibility` (0.15 weight — commercial viability)
     5. `location_distance` (0.10 weight — geographic proximity)
     6. `availability_time_compatibility` (0.15 weight — operational schedule reliability)
   - Realistic business rules and non-linear penalties:
     - Technical execution penalty: If `skill_match < 0.25`, steep penalty factor applied.
     - Schedule clash penalty: If `availability_time_compatibility < 0.15`, severe score reduction.
     - Creative synergy bonus: If both `skill_match >= 0.85` and `portfolio_relevance >= 0.85`, +3.0 score bonus.
     - Budget friction: If `budget_compatibility < 0.20`, 10% commercial penalty.
     - Controlled Gaussian perturbation ($\mu=0, \sigma=1.5$) modeling subtle studio preferences.
   - Target match score strictly bounded between 0.0 and 100.0 (ML-011).
   - Ratings strictly excluded from all training features (ML-005, RAT-004).
   - Saved to `ml-service/data/synthetic_training_data.csv`.

3. **Model Training & Hyperparameter Tuning Pipeline (ML-007, ML-008, ML-009)**:
   - Script: `ml-service/app/train.py`.
   - 80/20 train/test split (4,000 training, 1,000 test holdout).
   - 5-fold cross validation with `GridSearchCV` tuning tree regularization parameters:
     - `max_depth`: [4, 6, 8, 10, 12, None]
     - `min_samples_split`: [2, 5, 10, 20]
     - `min_samples_leaf`: [1, 2, 5, 10]
   - Optimal regularized hyperparameters selected:
     - `min_samples_leaf: 5`
     - `min_samples_split: 2`
     - `max_depth: None`
   - Evaluation Metrics:
     - Training: MAE = 1.56, RMSE = 2.03, $R^2 = 0.960$
     - 5-Fold Cross Validation: MAE = 3.17, RMSE = 4.01, $R^2 = 0.843$
     - Holdout Test (20%): MAE = 3.09, RMSE = 3.92, $R^2 = 0.847$
   - Feature Importances:
     - `skill_match`: 0.4488
     - `portfolio_relevance`: 0.2841
     - `budget_compatibility`: 0.1137
     - `location_distance`: 0.0756
     - `availability_time_compatibility`: 0.0684
     - `experience`: 0.0094
   - Model artifact serialized to `ml-service/app/models/decision_tree_model.joblib`.
   - Comprehensive metadata saved to `ml-service/app/models/model_metadata.json`.

4. **Inference Engine & Microservice Endpoints (ML-010, ML-011)**:
   - `ml-service/app/schemas.py`: Pydantic models with schema validation, range checks ($[0.0, 1.0]$), and dual snake_case / camelCase support.
   - `ml-service/app/predictor.py`: Singleton model manager with automated loading, input DataFrame formatting, and clipped score prediction.
   - `ml-service/app/main.py`:
     - `POST /predict-match`: Computes match score (0.0 to 100.0) for a single creator candidate.
     - `POST /predict-batch`: Batch vectorized prediction endpoint for ranking candidate lists from Spring Boot.
     - `GET /model-info`: Returns model architecture, hyperparameters, and evaluation metrics.
     - `GET /health`: Operational status and model loaded flag.

5. **Automated Test Suite**:
   - `ml-service/tests/test_ml_service.py` with 12 comprehensive unit and integration tests across data generation, model artifact loading, domain monotonicity, validation error handling, and single/batch API endpoints.

### Verification Summary
- **ML Test Suite**: `pytest` -> **12 of 12 tests PASSED (0 failures, 0 errors)** in 2.19s:
  1. `test_dataset_shape_and_features`: Verifies 5,000 records, 6 features, target score, and absence of ratings.
  2. `test_feature_and_target_bounds`: Verifies feature bounds $[0, 1]$ and target score bounds $[0, 100]$.
  3. `test_domain_weighting_monotonicity`: Verifies skill match correlation and domain rules.
  4. `test_model_artifact_type`: Verifies model is `DecisionTreeRegressor`.
  5. `test_model_metadata_metrics`: Verifies MAE, RMSE, and $R^2 \ge 0.70$.
  6. `test_feature_importances_exclude_ratings`: Verifies 6 features and exclusion of ratings.
  7. `test_health_check`: Verifies `status: UP` and `model_loaded: true`.
  8. `test_predict_match_snake_case`: Verifies single candidate prediction with snake_case fields.
  9. `test_predict_match_camel_case`: Verifies single candidate prediction with camelCase fields.
  10. `test_predict_match_validation_bounds_error`: Verifies HTTP 422 for out-of-range inputs ($> 1.0$, $< 0.0$).
  11. `test_predict_batch_endpoint`: Verifies batch candidate ranking and score relative ordering.
  12. `test_model_info_endpoint`: Verifies model metadata retrieval.
- **Backend Verification**: `mvn test-compile` -> **BUILD SUCCESS** (168 backend source files, 12 test files).
- **Frontend Verification**: `npm run build` -> **Compiled cleanly with 0 TypeScript errors in 7.33s**.






