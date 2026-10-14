# StudioLynk Architecture and Entity-Relationship Specification

## 1. High-Level System Architecture

The StudioLynk platform is structured as a decoupled three-tier system reverse-proxied by Nginx on an AWS EC2 instance (Ubuntu 24.04 LTS), backed by MySQL 8.0, Amazon S3 for cloud object storage, and a Python FastAPI Machine Learning microservice.

```mermaid
graph TD
    Client["Client Browser (Desktop / Mobile)"]
    
    subgraph AWS_EC2 ["AWS EC2 Ubuntu 24.04 LTS (Elastic IP: 13.207.83.159)"]
        Nginx["Nginx 1.24 Reverse Proxy (Port 80)"]
        
        subgraph Frontend_App ["Frontend (React 18 + TS + Tailwind + Vite)"]
            SPA["Single Page Application (dist/)"]
        end
        
        subgraph Backend_App ["Backend (Java 21 + Spring Boot 3.3.4 - Port 8080)"]
            Security["Spring Security + JJWT 0.12.6"]
            Controllers["REST Controllers (Auth, Studio, Freelancer, Portfolio, Shoot, etc.)"]
            WebSocketBroker["WebSocket / STOMP Broker (/ws)"]
            Services["Business Service Layer"]
            DataLayer["Spring Data JPA / Hibernate"]
            Flyway["Flyway Migration Engine"]
            MlClient["MlServiceClient (Spring RestClient)"]
            StorageAdapter["StorageService (S3 / Local Fallback)"]
        end
        
        subgraph ML_Microservice ["ML Microservice (Python 3.12 + FastAPI - Port 8000)"]
            FastAPI["FastAPI Daemon (systemd)"]
            Model["scikit-learn DecisionTreeRegressor"]
        end
        
        subgraph Database ["Local Isolated MySQL 8.0 (Port 3306)"]
            MySQLDB[("studiolynk DB (27 Normalized Tables)")]
        end
    end
    
    subgraph External_Services ["Managed External Cloud Services"]
        S3["AWS S3 Bucket: studiolynk-bucket-2026 (ap-south-1)"]
        SMTP["Gmail SMTP TLS Server (Port 587)"]
    end

    Client -->|HTTP / HTTPS Port 80| Nginx
    Client <-->|WebSocket STOMP /ws| Nginx
    
    Nginx -->|Route / | SPA
    Nginx -->|Proxy /api/ | Controllers
    Nginx -->|Proxy /ws | WebSocketBroker
    Nginx -->|Proxy /ml/ | FastAPI
    
    Controllers --> Security
    Security --> Services
    Services --> DataLayer
    Services --> MlClient
    Services --> StorageAdapter
    Services --> WebSocketBroker
    
    MlClient -->|HTTP 127.0.0.1:8000| FastAPI
    FastAPI --> Model
    
    DataLayer --> Flyway
    DataLayer -->|JDBC 127.0.0.1:3306| MySQLDB
    
    StorageAdapter -->|AWS SDK v2 HTTPS| S3
    Services -->|JavaMail TLS| SMTP
```

---

## 2. Component Interaction & Core Workflows

### 2.1 AI Requirement Matching & Candidate Discovery Flow

```mermaid
sequenceDiagram
    autonumber
    actor Studio as Studio User
    participant Frontend as React Frontend
    participant Gateway as Nginx Proxy (:80)
    participant Backend as Spring Boot (:8080)
    participant DB as MySQL DB (:3306)
    participant ML as FastAPI Service (:8000)
    participant Model as DecisionTree Model

    Studio->>Frontend: Open Requirement Detail -> Click "Find AI Matches"
    Frontend->>Gateway: GET /api/requirements/{id}/matching-freelancers
    Gateway->>Backend: Forward to RequirementController
    Backend->>DB: Query confirmed shoot requirement & parameters
    Backend->>DB: Query candidate freelancers in location scope
    Backend->>DB: Check rolling 10-day availability (AVL-004, AVL-007)
    Note over Backend,DB: Candidates with Busy or Not Set status on shoot date are filtered out
    
    loop For each eligible candidate
        Backend->>Backend: Extract 6 normalized features in [0.0, 1.0]
    end
    
    Backend->>ML: POST /predict-batch (features payload)
    ML->>Model: Vectorized batch inference (DecisionTreeRegressor)
    Model-->>ML: Predicted match scores in [0.0, 100.0]
    ML-->>Backend: Return ranked candidate IDs and scores
    Backend->>DB: Enrich with profile photos, gear highlights, reviews
    Backend-->>Gateway: HTTP 200 OK (Ranked Candidates)
    Gateway-->>Frontend: Display AI recommended candidate cards
```

---

## 3. Complete Entity-Relationship (ER) Schema

StudioLynk operates on **27 normalized relational tables** in MySQL 8.0, maintained with strict foreign key constraints, primary keys, audit timestamps, and indexing.

```mermaid
erDiagram
    users ||--o| studios : "owns (1:1)"
    users ||--o| freelancers : "owns (1:1)"
    users ||--o{ password_reset_otps : "requests (1:N)"
    users ||--o{ notifications : "receives (1:N)"
    users ||--o{ ratings : "authors / receives (1:N)"

    studios ||--o{ studio_social_links : "has (1:N)"
    studios ||--o{ studio_identity_submissions : "submits (1:N)"
    studios ||--o{ work_requirements : "creates (1:N)"

    freelancers ||--o| portfolios : "maintains (1:1)"
    freelancers ||--o{ freelancer_availability : "schedules (1:N)"
    freelancers ||--o{ freelancer_skills : "possesses (N:M)"
    freelancers ||--o{ freelancer_services : "offers (N:M)"
    freelancers ||--o{ freelancer_equipment : "owns (N:M)"
    freelancers ||--o{ work_requests : "receives (1:N)"

    skills ||--o{ freelancer_skills : "categorizes"
    skills ||--o{ work_requirement_skills : "required by"

    services ||--o{ freelancer_services : "categorizes"
    services ||--o{ work_requirement_services : "required by"

    equipment_categories ||--o{ equipment : "groups"
    equipment ||--o{ freelancer_equipment : "inventoried in"
    equipment ||--o{ work_requirement_equipment : "required in"

    portfolios ||--o{ portfolio_categories : "contains (1:N)"
    portfolio_categories ||--o{ portfolio_images : "contains (1:N)"

    work_requirements ||--o{ work_requirement_skills : "specifies (N:M)"
    work_requirements ||--o{ work_requirement_services : "specifies (N:M)"
    work_requirements ||--o{ work_requirement_equipment : "specifies (N:M)"
    work_requirements ||--o{ work_requests : "generates (1:N)"
    work_requirements ||--o{ conversations : "scoped to (1:N)"
    work_requirements ||--o{ ratings : "evaluated in (1:N)"

    work_requests }o--|| freelancers : "sent to"
    conversations ||--o{ messages : "contains (1:N)"

    users {
        bigint id PK
        varchar email UK
        varchar password_hash
        varchar role "STUDIO | FREELANCER | ADMIN"
        boolean is_active
        boolean onboarding_completed
        timestamp created_at
        timestamp updated_at
    }

    studios {
        bigint id PK
        bigint user_id FK, UK
        varchar studio_name
        varchar owner_name
        varchar phone
        text address
        decimal latitude
        decimal longitude
        int years_of_operation
        varchar logo_url
        timestamp created_at
        timestamp updated_at
    }

    freelancers {
        bigint id PK
        bigint user_id FK, UK
        varchar full_name
        varchar phone
        text address
        decimal latitude
        decimal longitude
        int experience_years
        text bio
        varchar profile_photo_url
        decimal full_day_rate
        decimal half_day_rate
        timestamp created_at
        timestamp updated_at
    }

    portfolios {
        bigint id PK
        bigint freelancer_id FK, UK
        timestamp created_at
        timestamp updated_at
    }

    portfolio_categories {
        bigint id PK
        bigint portfolio_id FK
        varchar name
        int sort_order
        timestamp created_at
        timestamp updated_at
    }

    portfolio_images {
        bigint id PK
        bigint category_id FK
        varchar s3_key
        varchar image_url
        varchar original_filename
        varchar content_type
        bigint file_size
        int sort_order
        timestamp created_at
    }

    freelancer_availability {
        bigint id PK
        bigint freelancer_id FK
        date date
        varchar status "AVAILABLE | BUSY | NOT_SET"
        time start_time
        time end_time
        timestamp updated_at
    }

    work_requirements {
        bigint id PK
        bigint studio_id FK
        varchar title
        varchar event_type
        date shoot_date
        time start_time
        time end_time
        text venue_address
        varchar day_type "FULL_DAY | HALF_DAY"
        decimal budget
        text brief
        varchar status "DRAFT|OPEN|REQUESTED|CONFIRMED|COMPLETED|CANCELLED"
        varchar client_name
        varchar client_phone
        bigint confirmed_freelancer_id FK
        timestamp created_at
        timestamp updated_at
    }

    work_requests {
        bigint id PK
        bigint work_requirement_id FK
        bigint freelancer_id FK
        varchar status "PENDING|ACCEPTED|REJECTED|CONFIRMED|CANCELLED"
        decimal agreed_rate
        text message
        text cancellation_reason
        timestamp created_at
        timestamp updated_at
    }

    conversations {
        bigint id PK
        bigint work_requirement_id FK
        bigint freelancer_id FK
        timestamp created_at
        timestamp updated_at
    }

    messages {
        bigint id PK
        bigint conversation_id FK
        bigint sender_id FK
        text content
        boolean is_read
        timestamp created_at
    }

    notifications {
        bigint id PK
        bigint user_id FK
        varchar type
        varchar title
        text content
        varchar link_url
        boolean is_read
        timestamp created_at
    }

    ratings {
        bigint id PK
        bigint work_requirement_id FK
        bigint reviewer_id FK
        bigint reviewee_id FK
        int score
        text review_text
        timestamp created_at
    }
```

---

## 4. Key Architectural Safeguards & Design Decisions

1. **Private MySQL Isolation**: MySQL 8.0 listens strictly on loopback interface `127.0.0.1:3306`. It cannot be reached from the public internet.
2. **Reverse Proxying with Protocol Upgrade**: Nginx on Port 80 manages SPA routing with fallback to `/index.html`, and proxies WebSocket requests to `/ws` with HTTP `Upgrade` and `Connection` headers.
3. **Pluggable Storage Abstraction**: `StorageService` interface decouples controllers from physical storage. The system automatically binds `S3StorageServiceImpl` when AWS credentials and bucket parameters are configured, and gracefully falls back to `LocalStorageServiceImpl` for local/offline testing.
4. **Autonomous Onboarding**: Studio and Freelancer users gain immediate platform access upon completing onboarding without administrative verification bottlenecks.
5. **Client Privacy Protection**: Direct client names and phone numbers are encrypted/masked and never surfaced to competing freelancers or public feeds; they are exclusively revealed to the single confirmed creator upon studio confirmation.
