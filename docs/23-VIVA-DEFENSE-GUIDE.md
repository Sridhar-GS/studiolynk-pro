# StudioLynk Academic Viva & Project Defense Guide

This guide prepares project authors for final-year engineering viva voce examinations, external faculty reviews, and technical project defense sessions. It addresses the architectural rationale, algorithm design decisions, security choices, and deliberate scope constraints behind StudioLynk.

---

## 1. Core Problem Statement & Architecture

### Q1: What is the core problem StudioLynk solves, and what is its novelty?
**Examiner Answer**:
Commercial photography studios face acute bottlenecks during peak wedding and event seasons: they need temporary specialized talent (cinematographers, drone operators, traditional portrait specialists) on specific dates, within tight call hours, and matching exact camera gear standards. 

Existing generic freelance platforms (Upwork, Fiverr) fail because:
1. They lack date-and-hour interval scheduling matching.
2. They do not inventory professional camera gear (e.g. Sony FX3, 70-200mm f/2.8 GM lenses, gimbal stabilizers).
3. They lack domain-specific AI matching that understands the difference between a candid wedding shooter and a commercial fashion photographer.

StudioLynk introduces a dedicated B2B coordination ecosystem connecting photography studios with verified freelance creators using a deterministic machine learning recommendation pipeline and rolling availability scheduling.

---

### Q2: Why did you choose a three-tier architecture with a separate Python microservice instead of a single monolith?
**Examiner Answer**:
The architecture reflects separation of concerns aligned with specialized technology strengths:
- **Backend (Spring Boot / Java 21)**: Ideal for mission-critical relational business transactions, multi-role security (Spring Security + JWT), database migrations (Flyway), and bidirectional WebSocket/STOMP socket handling.
- **ML Microservice (FastAPI / Python 3.12)**: Python is the industry standard for scientific computation and machine learning (`scikit-learn`, `numpy`, `pandas`). Running it as a dedicated lightweight microservice allows independent model retraining and fast vectorized batch matrix operations without weighing down the JVM.
- **Frontend (React / Vite / TypeScript)**: Provides an ultra-responsive, type-safe single-page interface with instant client-side state transitions.
- **Nginx Reverse Proxy**: Solves Cross-Origin Resource Sharing (CORS) cleanly by serving all tiers under a unified origin on Port 80, handling protocol upgrade headers for WebSockets.

---

## 2. Machine Learning & Algorithm Justification

### Q3: Why did you choose a Decision Tree Regressor instead of a Deep Neural Network or Random Forest?
**Examiner Answer**:
1. **Explainability & Transparency**: In an academic project defense, black-box neural networks cannot be audited. A Decision Tree provides transparent binary split thresholds (e.g. `skill_match <= 0.65`) that map directly to human-understandable hiring decisions.
2. **Computational Footprint**: A decision tree executes in sub-millisecond time ($O(\text{depth})$) with negligible memory footprint, running smoothly on a modest 4GB RAM EC2 server without requiring costly GPU instances.
3. **Non-Linear Hiring Boundaries**: Studio hiring decisions feature sharp non-linear thresholds (e.g. lack of critical skills or shoot date unavailability immediately drops suitability). Decision trees handle non-linear step-functions naturally without artificial kernel tricks.

---

### Q4: Why are user star ratings strictly excluded from the ML feature vector?
**Examiner Answer**:
Excluding star ratings from the ML model is a deliberate architectural and ethical design decision:
1. **Mitigating Cold-Start Inequity**: If ratings influenced recommendation scores, newly registered talented creators with zero reviews would receive low match scores, creating a "rich-get-richer" monopoly for established users.
2. **Feedback Loop Prevention**: A model that boosts high-rated creators will continuously recommend only those creators, depriving the system of fresh exploration and entrenching popularity bias.
3. **Objective Compatibility**: A creator's suitability for a shoot depends on objective technical factors: **gear ownership, skill alignment, location proximity, budgetary fit, and calendar availability**, rather than subjective post-event ratings.
4. Ratings remain visible on public profile cards for human studios to review subjectively after candidates are objectively ranked.

---

### Q5: Why did you not implement Computer Vision (CV) to analyze portfolio image aesthetics?
**Examiner Answer**:
1. **Subjectivity of Photographic Art**: Computer vision algorithms struggle to evaluate artistic photography (e.g., moody low-key portraits vs high-key commercial lighting).
2. **Resource Constraints**: Image deep learning models (ResNet, Vision Transformers) require substantial GPU acceleration and memory, which violates the lightweight academic prototype constraint.
3. **Metadata Sufficiency**: StudioLynk uses structured category tagging and semantic token matching (`POR-009`, `ML-004`), which accurately captures the creator's specialized domain (e.g., "Traditional Weddings", "Drone Aerials") with zero latency.

---

### Q6: What happens if the Python ML microservice becomes unavailable?
**Examiner Answer**:
StudioLynk implements **resilience through graceful degradation**:
- In `AiMatchingService.java`, the Spring Boot client wraps the ML call with strict connect (2s) and read (3s) timeouts.
- If the ML service is down or times out, the backend automatically falls back to a deterministic, rule-based domain scoring algorithm (`calculateDomainFallbackScore()`).
- The user interface continues operating smoothly with zero service interruption.

---

## 3. Workflows, Scheduling & Privacy

### Q7: Explain the rolling 10-day availability calendar. Why not allow scheduling months in advance?
**Examiner Answer**:
Freelance photographers operate in a highly dynamic, short-notice gig market where long-range commitments lead to frequent cancellations:
1. **Active Commitment Window**: Enforcing a rolling 10-day window (`today` to `today + 9 days`) guarantees that availability statuses (`AVAILABLE`, `BUSY`, `NOT_SET`) reflect current commitments.
2. **Hard Filtering Foundation**: Candidates marked as `BUSY` or `NOT_SET` on the shoot date are hard-filtered before the ML pipeline is invoked (`AVL-007`, `ML-012`), ensuring studios never waste negotiation time on unavailable creators.
3. **Beyond 10 Days**: Shoots scheduled beyond 10 days display matching candidates with an explicit "Availability Unknown" status (`AVL-006`), prompting the studio to initiate real-time communication to confirm schedule alignment.

---

### Q8: How is client privacy protected during the discovery and negotiation phase?
**Examiner Answer**:
Under requirements `REQ-002` and `REQ-006`:
- Shoot requirements often involve sensitive client information (e.g. Bride/Groom full names, direct personal mobile numbers, private venue access codes).
- In open feeds, candidate search cards, and negotiation chats, all client personal identifying information (PII) is masked and withheld by the backend.
- Direct client contacts are unlocked exclusively for the **one confirmed freelancer** after the studio formally approves the booking (`REQ-006`).

---

### Q9: Why is there only one confirmed freelancer per shoot in the prototype?
**Examiner Answer**:
Studio shoots in our target domain operate on single-operator responsibilities (e.g., one Lead Candid Photographer). To avoid multi-booking race conditions and logistical conflicts, the system allows studios to dispatch requests to multiple candidate creators, negotiate rates via real-time WebSocket messaging, and confirm one chosen creator (`WRK-007`). Confirming one creator automatically cancels all other pending competing requests (`WRK-008`).

---

## 4. Security, Storage & Infrastructure

### Q10: Why did you host MySQL on the EC2 instance instead of AWS RDS?
**Examiner Answer**:
1. **Academic Cost Efficiency**: AWS RDS introduces substantial continuous billing overhead. Co-locating MySQL 8.0 on the Ubuntu EC2 instance keeps the college project cost strictly within free/low-tier limits.
2. **Security Isolation**: MySQL is configured to bind strictly to loopback `127.0.0.1:3306`, completely invisible to the outside internet, with local database credentials managed securely via private environment variables (`AWS-009`).
3. **Zero Complexity**: A single instance simplifies deployment, backup snapshots, and local database seeding.

---

### Q11: How is Amazon S3 organized and secured?
**Examiner Answer**:
- **Bucket**: `studiolynk-bucket-2026` in `ap-south-1`.
- **Entity-Centric Hierarchy**: All assets are logically isolated per account (`studios/{studioId}/...` and `freelancers/{freelancerId}/...`).
- **Category Slugs & Timestamps**: S3 keys use human-readable slugs and millisecond timestamps (e.g., `freelancers/5/portfolio/12_wedding_highlights/1792005042341_candid_portrait.jpg`), ensuring chronological sorting and zero name collisions.
- **Validation**: Multipart uploads are validated by the backend for MIME types (`JPEG`, `PNG`, `WEBP`) and a 10 MB size limit before streaming to S3.
- **Public Policy**: Objects have public read permissions for fast CDN/browser asset rendering, while AWS access credentials remain private on the server.

---

### Q12: Why does the Admin Platform have a read-only architecture?
**Examiner Answer**:
Under requirements `ADM-003` and `ADM-004`:
1. **Operational Integrity**: StudioLynk is an autonomous platform where studios and freelancers conduct direct business. Administrators should audit system health, review user counts, and inspect identity declarations, but should not tamper with agreed shoot rates, private chat histories, or active bookings.
2. **Security**: Read-only administration prevents accidental operational data corruption and reduces the attack surface if an administrative credential were compromised. Any attempted POST/PUT/DELETE on administrative operational views returns `HTTP 405 Method Not Allowed`.

---

## 5. Defense Summary Checklist

| Academic Criteria | How StudioLynk Satisfies It |
| :--- | :--- |
| **System Complexity** | Full-stack three-tier architecture (React + Spring Boot + FastAPI + MySQL + S3 + Nginx). |
| **Database Design** | 27 normalized relational tables with foreign key cascades, unique constraints, and Flyway migration tracking. |
| **Algorithmic Depth** | Custom Decision Tree Regressor trained on 5,000 domain records with 6 normalized continuous features and 5-fold cross-validation. |
| **Real-Time Capabilities** | Bidirectional WebSocket/STOMP protocol for real-time negotiations and notification pushing. |
| **Cloud Deployment** | Production deployment on AWS EC2 Ubuntu 24.04 with Nginx reverse proxy, systemd daemons, and AWS S3 integration. |
| **Adherence to Constraints** | No unauthorized enterprise bloat (Kubernetes, Kafka, Redis, LLMs, or Payment Gateways), maintaining a rock-solid, understandable academic prototype. |
