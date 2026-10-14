# StudioLynk — Photography Studio & Freelancer Network

StudioLynk is a comprehensive, production-ready final-year academic prototype platform designed to bridge commercial photography studios and freelance creators. It integrates multi-parameter freelancer discovery, Decision Tree machine learning match scoring, rolling 10-day availability scheduling, real-time WebSocket/STOMP negotiation messaging, two-way mutual reviews, and an administrative monitoring console.

---

## Architecture Overview

```
                      +---------------------------------------+
                      |               Internet                |
                      +---------------------------------------+
                                          |
                                          | HTTP (Port 80)
                                          v
                      +---------------------------------------+
                      |         AWS EC2 Ubuntu 24.04          |
                      |          (Elastic IP Demo)            |
                      |                                       |
                      |   +-------------------------------+   |
                      |   |      Nginx Reverse Proxy      |   |
                      |   +-------------------------------+   |
                      |       |           |           |       |
                 /----+       |           |           +----\  |
                 |            | /api/     | /ws            |  |
                 v            v           v                v  |
         +---------------+ +-------------------------+ +----+ |
         | React 18 SPA  | | Spring Boot 3.3 Backend | | ML | |
         | (dist/ assets)| | (Port 8080, Java 21)    | |(8000)|
         +---------------+ +-------------------------+ +----+ |
                                   |              |           |
                                   v              v           |
                       +----------------+ +-----------------+ |
                       | MySQL 8.0 DB   | | AWS S3 Bucket   | |
                       | (127.0.0.1:3306| | (Object Storage)| |
                       | Private to EC2)| | (ap-south-1)    | |
                       +----------------+ +-----------------+ |
                      +---------------------------------------+
```

---

## Technology Stack

- **Frontend:** React 18, TypeScript 5, Vite 5, Tailwind CSS 3.4, Lucide React icons, Axios.
- **Backend:** Java 21 LTS, Spring Boot 3.3.4, Spring Security, JJWT (0.12.6), Spring Data JPA, Flyway Migration Engine.
- **Database:** MySQL 8.0 with 27 normalized relational tables bound strictly to `127.0.0.1:3306`.
- **Machine Learning Microservice:** Python 3.12, FastAPI, scikit-learn (`DecisionTreeRegressor`), pandas, NumPy, Pydantic.
- **Messaging:** Bidirectional WebSocket / STOMP (`/ws`, `/topic/conversation.{id}`, `/app/chat.sendMessage`).
- **Cloud Infrastructure:** AWS EC2 (Ubuntu 24.04 LTS), Elastic IP (`13.207.83.159`), AWS S3 (`ap-south-1`), Nginx 1.24.

---

## Security Architecture

1. **Private Database Isolation (AWS-009):** MySQL port 3306 is bound exclusively to `127.0.0.1` and never exposed to the public internet.
2. **Reverse Proxy Consolidation (AWS-008):** Nginx on port 80 acts as the single public entry point, routing requests internally to static assets, Spring Boot (8080), and FastAPI (8000).
3. **Authentication & Authorization (AUTH-001 - AUTH-008):** BCrypt password hashing, JJWT bearer tokens with 24-hour expiration, and role-based access control (`ROLE_STUDIO`, `ROLE_FREELANCER`, `ROLE_ADMIN`).
4. **Autonomous Access (STU-004, FRL-007, ADM-006):** Studios and Freelancers enter and operate on the platform immediately upon onboarding completion without requiring administrative gatekeeping.
5. **Admin Monitoring Integrity (ADM-003, ADM-004):** Administrative operations are strictly view-only, rejecting all data mutation requests with HTTP 405 Method Not Allowed.

---

## Demonstration Accounts & Credentials

The platform includes realistic seeded accounts across all three user roles with pre-configured profiles, portfolios, 10-day availability windows, requirement histories, and mutual ratings:

| Role | Name / Brand | Email | Password | Location / Specialization |
| :--- | :--- | :--- | :--- | :--- |
| **Admin** | System Auditor | `admin@studiolynk.com` | `Admin@2026!` | Global View-Only Auditor |
| **Studio** | Studio Lumière | `studio.lumiere@studiolynk.com` | `Demo@2026!` | Chennai (Adyar) &bull; Commercial / Wedding |
| **Studio** | Monochrome Media | `studio.monochrome@studiolynk.com` | `Demo@2026!` | Bangalore (Indiranagar) &bull; Fashion / Editorial |
| **Freelancer** | Karthik Lens | `karthik.lens@studiolynk.com` | `Demo@2026!` | Chennai &bull; Candid Wedding & Drone &bull; Sony A7IV |
| **Freelancer** | Ananya Visuals | `ananya.visuals@studiolynk.com` | `Demo@2026!` | Chennai &bull; Fashion & Newborn &bull; Canon R5 |
| **Freelancer** | Vignesh Cinema | `vignesh.cinema@studiolynk.com` | `Demo@2026!` | Chennai &bull; Cinematic Video & Gimbals &bull; Sony FX3 |
| **Freelancer** | Deepak Shoots | `deepak.shoots@studiolynk.com` | `Demo@2026!` | Bangalore &bull; Destination & Wildlife &bull; Nikon Z8 |

> **Login Portal URL:** `http://<ELASTIC_IP>/login` (or `http://localhost:5173/login` in dev mode)

---

## Academic Documentation Index

StudioLynk includes a comprehensive suite of academic artifacts and engineering specifications:

| Doc # | Document Title | Description |
| :--- | :--- | :--- |
| **19** | [Architecture & ER Diagrams](docs/19-ARCHITECTURE-AND-ER-DIAGRAMS.md) | Multi-tier C4 architecture, AI matching sequence flow, and complete 27-table relational ER diagram. |
| **20** | [REST API Catalogue](docs/20-API-CATALOGUE.md) | Full endpoint specification across 11 functional domains with auth scopes, schemas, and status codes. |
| **21** | [ML Methodology Report](docs/21-ML-METHODOLOGY-REPORT.md) | Mathematical formulation, 6 normalized features, Decision Tree tuning, $R^2=0.843$ metrics, and ethical fairness rules. |
| **22** | [Test Case Specification](docs/22-TEST-CASE-SPECIFICATION.md) | Comprehensive test matrix covering 87 JUnit backend tests, 12 FastAPI pytest tests, and Vite build probes. |
| **23** | [Viva Defense Guide](docs/23-VIVA-DEFENSE-GUIDE.md) | 15 anticipated university examination questions and defensible academic responses. |
| **Demo** | [Academic SQL Seed](backend/src/main/resources/db/demo/demo_academic_seed.sql) | Comprehensive SQL seed script populating studios, creators, portfolios, shoots, and chat records. |
| **01-18** | [Specifications & Architecture](docs/) | Foundation documents covering requirements, data dictionary, UI/UX, security, and architectural decisions. |

---

## Machine Learning Match Scoring (Phase 14 & 15)

- **Algorithm:** Decision Tree Regressor (`sklearn.tree.DecisionTreeRegressor`)
- **Dataset:** 5,000 synthetic records with explicit domain logic.
- **Features (Exactly 6):**
  1. `skill_match` $[0.0, 1.0]$: Matching of required shoot skills against freelancer skills.
  2. `portfolio_relevance` $[0.0, 1.0]$: Text & category alignment without computer vision.
  3. `experience` $[0.0, 1.0]$: Normalized craft experience $\min(1.0, \text{exp} / 10.0)$.
  4. `budget_compatibility` $[0.0, 1.0]$: Rate compatibility with non-linear penalties for over-budget rates.
  5. `location_distance` $[0.0, 1.0]$: Spherical Haversine proximity $\max(0, 1 - \text{dist}/50.0)$.
  6. `availability_time_compatibility` $[0.0, 1.0]$: Rolling 10-day window slot containment.
- **Hard Availability Filtering (ML-012):** Candidates who are Busy or Not Set on the shoot date are strictly excluded prior to ranking.
- **Fairness Guarantee (ML-005, RAT-004):** User ratings are strictly excluded from ML features.

---

## Services & Operational Management

### 1. Nginx Reverse Proxy
```bash
sudo systemctl status nginx
sudo nginx -t
sudo systemctl reload nginx
```

### 2. Python FastAPI ML Microservice
```bash
# Manage via systemd:
sudo systemctl status studiolynk-ml.service
sudo systemctl restart studiolynk-ml.service

# Run test suite:
cd ml-service
./venv/bin/pytest tests/test_ml_service.py
```

### 3. Frontend Development & Build
```bash
cd frontend
npm install
npm run build      # Production bundling to dist/
npm run dev        # Development server
```

### 4. MySQL Database Management
```bash
# Connect to private MySQL instance:
mysql -u studiolynk_user -pstudiolynk_local_pass_2026 studiolynk
```

---

## License & Academic Disclaimer

Built for academic prototype submission and demonstration. All rights reserved &copy; 2026 StudioLynk.
