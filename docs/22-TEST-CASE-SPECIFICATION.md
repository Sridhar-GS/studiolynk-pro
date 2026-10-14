# StudioLynk Test Case Specification & Verification Matrix

This document provides the formal test case matrix for the StudioLynk academic prototype, verifying functional, non-functional, security, and integration requirements.

---

## 1. Test Execution Summary

| Test Category | Suite / Framework | Total Tests | Passed | Failed | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Backend Integration & Unit** | JUnit 5 + Spring Boot Test + MockMvc | 87 | 87 | 0 | **PASS (100%)** |
| **Machine Learning Microservice** | Pytest 9.1 + Starlette TestClient | 12 | 12 | 0 | **PASS (100%)** |
| **Frontend Compilation & Types** | TypeScript (`tsc`) + Vite Rollup | 1,688 modules | 1,688 | 0 | **PASS (100%)** |
| **AWS Cloud Storage Live Probes** | AWS SDK v2 + Boto3 S3 Client | 3 | 3 | 0 | **PASS (100%)** |
| **Database Migrations** | Flyway 10.17 Schema Validator | 2 migrations | 2 | 0 | **PASS (100%)** |

---

## 2. Requirement Verification Matrix

### 2.1 Authentication & Onboarding Access Gate
| ID | Requirement | Test Scenario | Execution Type | Result |
| :--- | :--- | :--- | :--- | :--- |
| `TC-AUTH-001` | Role-based Registration (ONB-001) | Register studio and freelancer with duplicate email prevention (HTTP 409) | Automated (JUnit) | PASS |
| `TC-AUTH-002` | Strong Password Rules (AUTH-007) | Reject passwords missing uppercase, digit, or special character (HTTP 400) | Automated (JUnit) | PASS |
| `TC-AUTH-003` | Single Login Portal (AUTH-002) | Authenticate user, receive signed JJWT token with role claims | Automated (JUnit) | PASS |
| `TC-AUTH-004` | 6-Digit SMTP OTP Flow (AUTH-003, 005) | Request OTP via Gmail SMTP; verify 5-min expiry and 60-sec cooldown | Automated (JUnit) | PASS |
| `TC-AUTH-005` | Onboarding Access Gate (ONB-002) | Incomplete onboarding user blocked from dashboard and redirected to wizard | Automated (MockMvc) | PASS |

### 2.2 Studio Onboarding & Identity Declaration
| ID | Requirement | Test Scenario | Execution Type | Result |
| :--- | :--- | :--- | :--- | :--- |
| `TC-STU-001` | Onboarding Submission (STU-001) | Submit operational studio details and persist draft state (ONB-004) | Automated (JUnit) | PASS |
| `TC-STU-002` | Identity Declaration (STU-002, 003) | Store owner declaration and document URL without external Aadhaar API | Automated (JUnit) | PASS |
| `TC-STU-003` | Immediate Unlock (STU-004) | Verify studio gains immediate dashboard access without admin approval | Automated (JUnit) | PASS |
| `TC-STU-004` | Completion Score (ONB-005) | Calculate deterministic completion score based on bio, coords, and logo | Automated (JUnit) | PASS |

### 2.3 Freelancer Profile, Gear & Catalogue
| ID | Requirement | Test Scenario | Execution Type | Result |
| :--- | :--- | :--- | :--- | :--- |
| `TC-FRL-001` | Master Catalogue (FRL-002, 003) | Retrieve skills, services, and equipment grouped by category | Automated (JUnit) | PASS |
| `TC-FRL-002` | Custom Catalogue Items (FRL-004) | Allow freelancer to create custom skills and equipment gear | Automated (JUnit) | PASS |
| `TC-FRL-003` | Pricing Rates & Bio (FRL-006) | Update full-day and half-day rates, bio, and operational coordinates | Automated (JUnit) | PASS |
| `TC-FRL-004` | Autonomous Onboarding (FRL-007) | Profile unlocks immediately upon onboarding submission | Automated (JUnit) | PASS |

### 2.4 Portfolio & AWS S3 Storage
| ID | Requirement | Test Scenario | Execution Type | Result |
| :--- | :--- | :--- | :--- | :--- |
| `TC-POR-001` | One Portfolio / User (POR-001) | Auto-create single portfolio instance per freelancer | Automated (JUnit) | PASS |
| `TC-POR-002` | Category CRUD (POR-002, 005) | Create, rename, and delete portfolio category tabs | Automated (JUnit) | PASS |
| `TC-POR-003` | Multi-Image Upload (POR-004, 008) | Upload image batch with MIME type validation (JPEG/PNG/WEBP $\le 10$ MB) | Automated (JUnit) | PASS |
| `TC-POR-004` | S3 Key Formatting (POR-008) | Verify S3 key structure `freelancers/{id}/portfolio/{catId}_{slug}/{time}_{file}` | Automated (Boto3/SDK) | PASS |
| `TC-POR-005` | Drag/Drop Reordering (POR-006) | Reorder photos and category tabs by sort order indices | Automated (JUnit) | PASS |

### 2.5 Rolling 10-Day Availability & Conflict Detection
| ID | Requirement | Test Scenario | Execution Type | Result |
| :--- | :--- | :--- | :--- | :--- |
| `TC-AVL-001` | Rolling 10-Day Window (AVL-001) | Window slides automatically from `today` to `today + 9 days` | Automated (JUnit) | PASS |
| `TC-AVL-002` | Tri-State Status (AVL-002) | Track daily status across `AVAILABLE`, `BUSY`, and `NOT_SET` | Automated (JUnit) | PASS |
| `TC-AVL-003` | Time Validation (AVL-003) | Enforce start time before end time on available days | Automated (JUnit) | PASS |
| `TC-AVL-004` | Search Filtering (AVL-004, 005) | Strictly exclude Busy and Not Set candidates from 10-day search | Automated (JUnit) | PASS |
| `TC-AVL-005` | Beyond 10 Days (AVL-006) | Surface candidates beyond 10 days with "Availability Unknown" notice | Automated (JUnit) | PASS |
| `TC-AVL-006` | Booking Conflict (AVL-008, 009) | Confirmed work automatically marks shoot date as BUSY and prevents duplicate booking | Automated (JUnit) | PASS |

### 2.6 Work Requirements & Client Privacy Protection
| ID | Requirement | Test Scenario | Execution Type | Result |
| :--- | :--- | :--- | :--- | :--- |
| `TC-WRK-001` | Requirement Creation (WRK-001) | Create shoot requirement with event type, date, call hours, and budget | Automated (JUnit) | PASS |
| `TC-WRK-002` | Client Privacy Gate (REQ-002, 006) | Client name and phone number masked in public feed and candidate cards | Automated (JUnit) | PASS |
| `TC-WRK-003` | Requirement Lifecycle (WRK-003) | Transition requirement across DRAFT, OPEN, REQUESTED, CONFIRMED, COMPLETED | Automated (JUnit) | PASS |

### 2.7 Work Requests, Negotiation & Confirmation
| ID | Requirement | Test Scenario | Execution Type | Result |
| :--- | :--- | :--- | :--- | :--- |
| `TC-REQ-001` | Work Request Dispatch (REQ-001) | Studio dispatches candidate requests from discovery cards | Automated (JUnit) | PASS |
| `TC-REQ-002` | Accept / Reject Action (REQ-004) | Freelancer accepts or rejects shoot request with agreed rate | Automated (JUnit) | PASS |
| `TC-REQ-003` | Single Creator Confirmation (REQ-005) | Studio confirms one chosen creator | Automated (JUnit) | PASS |
| `TC-REQ-004` | Auto-Closure of Competitors (WRK-008) | Confirming one creator automatically cancels all other competing requests | Automated (JUnit) | PASS |
| `TC-REQ-005` | Privacy Unlock (REQ-006) | Direct client contact details unlocked exclusively for confirmed creator | Automated (JUnit) | PASS |
| `TC-REQ-006` | Cancellation Reason (REQ-007) | Mandatory reason text required to cancel confirmed booking | Automated (JUnit) | PASS |

### 2.8 Real-Time Chat & In-App Notifications
| ID | Requirement | Test Scenario | Execution Type | Result |
| :--- | :--- | :--- | :--- | :--- |
| `TC-MSG-001` | STOMP Transport (MSG-002) | Real-time bidirectional messaging via `/app/chat.sendMessage` and `/topic/conversation.{id}` | Automated (JUnit/STOMP) | PASS |
| `TC-MSG-002` | Thread Scoping (MSG-001) | Exactly one conversation thread per (requirement, freelancer) pair | Automated (JUnit) | PASS |
| `TC-NOT-001` | In-App Event Push (NOT-002) | Push real-time notification on request received, accepted, confirmed, and completed | Automated (JUnit) | PASS |
| `TC-NOT-002` | Read Marking (NOT-004) | Patch individual read and bulk read-all marks | Automated (JUnit) | PASS |

### 2.9 Two-Way Ratings & Reviews
| ID | Requirement | Test Scenario | Execution Type | Result |
| :--- | :--- | :--- | :--- | :--- |
| `TC-RAT-001` | Post-Completion Gate (RAT-001) | Ratings strictly rejected (HTTP 400) if shoot is not COMPLETED | Automated (JUnit) | PASS |
| `TC-RAT-002` | Duplicate Prevention (RAT-006) | Enforce unique constraint; second rating attempt returns HTTP 409 Conflict | Automated (JUnit) | PASS |
| `TC-RAT-003` | Rating Range (RAT-005) | Bean Validation enforces scores strictly in 1..5 range | Automated (JUnit) | PASS |
| `TC-RAT-004` | Public Display (RAT-003) | Compute average rating score and display verified review comments | Automated (JUnit) | PASS |

### 2.10 Machine Learning & AI Matching
| ID | Requirement | Test Scenario | Execution Type | Result |
| :--- | :--- | :--- | :--- | :--- |
| `TC-ML-001` | 6 Feature Normalization (ML-004) | Spring Boot computes normalized features strictly in $[0.0, 1.0]$ | Automated (JUnit) | PASS |
| `TC-ML-002` | Availability Hard Filter (ML-012) | Exclude busy candidates prior to invoking ML prediction | Automated (JUnit) | PASS |
| `TC-ML-003` | Batch Prediction Endpoint (ML-010) | FastAPI `/predict-batch` returns ranked list sorted by match score | Automated (Pytest) | PASS |
| `TC-ML-004` | Bounded Score Range (ML-011) | Predicted match scores strictly bounded to $[0.0, 100.0]$ | Automated (Pytest) | PASS |
| `TC-ML-005` | Ratings Exclusion (ML-005, RAT-004) | Altering creator rating yields zero change to ML match score | Automated (JUnit) | PASS |

### 2.11 Administrative Monitoring Platform
| ID | Requirement | Test Scenario | Execution Type | Result |
| :--- | :--- | :--- | :--- | :--- |
| `TC-ADM-001` | Role Protection (ADM-001) | Non-admin users receiving HTTP 403 Forbidden on `/api/admin/**` | Automated (JUnit) | PASS |
| `TC-ADM-002` | Read-Only Invariance (ADM-003, 004) | PUT/POST/DELETE calls on operational data return HTTP 405 Method Not Allowed | Automated (JUnit) | PASS |
| `TC-ADM-003` | Identity Audit (ADM-005) | View studio owner declaration records and document links | Automated (JUnit) | PASS |
| `TC-ADM-004` | Zero Bottleneck (ADM-006) | Unverified studios and freelancers onboard autonomously without admin bottleneck | Automated (JUnit) | PASS |
