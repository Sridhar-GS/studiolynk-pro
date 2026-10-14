# StudioLynk REST API Catalogue & Specification

This document provides the complete API catalogue for the StudioLynk academic prototype. All backend endpoints follow standard REST conventions, accept and produce `application/json` (unless handling multipart file uploads), and return standardized response envelopes.

---

## 1. Standard Response Formats

### 1.1 Success Response Envelope
```json
{
  "success": true,
  "message": "Operation completed successfully.",
  "data": { ... },
  "timestamp": "2026-10-14T19:24:39.295Z"
}
```

### 1.2 Error Response Envelope
```json
{
  "success": false,
  "message": "Human-readable error description.",
  "errors": ["Specific field error 1", "Specific field error 2"],
  "timestamp": "2026-10-14T19:24:39.295Z"
}
```

---

## 2. Authentication Endpoints (`/api/auth`)

| Method | Endpoint | Access | Summary | Request Body |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Public | Register new Studio or Freelancer account | `{"email", "password", "role": "STUDIO\|FREELANCER"}` |
| `POST` | `/api/auth/login` | Public | Authenticate user and receive JWT bearer token | `{"email", "password"}` |
| `POST` | `/api/auth/forgot-password/send-otp` | Public | Generate 6-digit OTP and send via Gmail SMTP | `{"email"}` |
| `POST` | `/api/auth/forgot-password/verify-otp` | Public | Validate OTP within 5-minute expiry window | `{"email", "otp"}` |
| `POST` | `/api/auth/forgot-password/reset` | Public | Reset account password using verified OTP | `{"email", "otp", "newPassword"}` |

---

## 3. Studio Management Endpoints (`/api/studios`)

| Method | Endpoint | Access | Summary |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/studios/me` | `ROLE_STUDIO` | Retrieve authenticated studio profile and onboarding draft |
| `POST` | `/api/studios/onboarding` | `ROLE_STUDIO` | Submit studio onboarding draft or final verification declaration |
| `PUT` | `/api/studios/me` | `ROLE_STUDIO` | Update studio profile information (name, address, coordinates, logo, social links) |
| `GET` | `/api/studios/{id}` | Authenticated | Retrieve public studio details |
| `GET` | `/api/studios/{id}/public-profile` | Public | Public landing profile with studio reviews and operational info |

---

## 4. Freelancer Profile & Discovery Endpoints (`/api/freelancers`)

| Method | Endpoint | Access | Summary |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/freelancers/me` | `ROLE_FREELANCER` | Retrieve authenticated freelancer profile, rates, and gear inventory |
| `POST` | `/api/freelancers/onboarding` | `ROLE_FREELANCER` | Submit freelancer onboarding (skills, services, equipment, pricing) |
| `PUT` | `/api/freelancers/me` | `ROLE_FREELANCER` | Update profile information, bio, rates, and catalogue selections |
| `GET` | `/api/freelancers/{id}` | Authenticated | Retrieve detailed freelancer profile for studio evaluation |
| `GET` | `/api/freelancers/search` | `ROLE_STUDIO` | Multi-parameter search filter (keyword, location distance, skills, rates, availability) |

---

## 5. Portfolio & AWS S3 Storage Endpoints (`/api/portfolio`, `/api/upload`)

| Method | Endpoint | Access | Summary | Request / Form Data |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/portfolio/me` | `ROLE_FREELANCER` | Auto-create and retrieve authenticated freelancer portfolio | None |
| `POST` | `/api/portfolio/categories` | `ROLE_FREELANCER` | Create custom portfolio category | `{"name": "Wedding Shoots"}` |
| `PUT` | `/api/portfolio/categories/{id}` | `ROLE_FREELANCER` | Edit category title | `{"name": "Candid Weddings"}` |
| `DELETE` | `/api/portfolio/categories/{id}` | `ROLE_FREELANCER` | Delete category and all associated S3 images | None |
| `POST` | `/api/portfolio/categories/{id}/images` | `ROLE_FREELANCER` | Upload multi-image batch to S3 category folder | `multipart/form-data` (`files[]`) |
| `DELETE` | `/api/portfolio/images/{id}` | `ROLE_FREELANCER` | Remove image from database and delete S3 object | None |
| `PUT` | `/api/portfolio/categories/{id}/reorder` | `ROLE_FREELANCER` | Reorder images by sort order IDs | `{"itemIds": [3, 1, 2]}` |
| `PUT` | `/api/portfolio/categories/reorder` | `ROLE_FREELANCER` | Reorder category tabs by sort order IDs | `{"itemIds": [2, 1]}` |
| `GET` | `/api/portfolio/freelancer/{id}` | Public / Authenticated | Public view of freelancer categories and photos | None |
| `POST` | `/api/upload/image` | Authenticated | Upload profile avatar or studio logo directly to S3 | `multipart/form-data` (`file`) |

---

## 6. Rolling 10-Day Availability Endpoints (`/api/freelancers`)

| Method | Endpoint | Access | Summary |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/freelancers/me/availability` | `ROLE_FREELANCER` | Retrieve rolling 10-day scheduling window (`today` to `today + 9 days`) |
| `PUT` | `/api/freelancers/me/availability` | `ROLE_FREELANCER` | Update daily status (`AVAILABLE`, `BUSY`, `NOT_SET`) and working call hours |
| `GET` | `/api/freelancers/{id}/availability` | `ROLE_STUDIO` | Inspect candidate's 10-day calendar availability |
| `POST` | `/api/freelancers/availability/check` | Authenticated | Check if a freelancer is available for a specified date and call time interval |

---

## 7. Work Requirements & Shoot Specifications (`/api/requirements`)

| Method | Endpoint | Access | Summary |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/requirements` | `ROLE_STUDIO` | List all shoot requirements owned by authenticated studio |
| `POST` | `/api/requirements` | `ROLE_STUDIO` | Create new shoot requirement with skills, services, gear, and budget |
| `GET` | `/api/requirements/{id}` | Authenticated | Retrieve requirement detail (client contacts masked for non-confirmed creators) |
| `PUT` | `/api/requirements/{id}` | `ROLE_STUDIO` | Update shoot specifications while requirement is in draft or open state |
| `PATCH` | `/api/requirements/{id}/status` | `ROLE_STUDIO` | Transition status (`OPEN`, `CANCELLED`, `COMPLETED`) |
| `GET` | `/api/requirements/{id}/matching-freelancers` | `ROLE_STUDIO` | **AI Matching Pipeline**: Hard availability filtering + FastAPI batch prediction |
| `GET` | `/api/requirements/public/open` | Public / Freelancers | Open requirements feed available for candidate discovery |

---

## 8. Work Requests & Booking Confirmation (`/api/requests`)

| Method | Endpoint | Access | Summary |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/requests` | `ROLE_STUDIO` | Send booking request to candidate freelancer from discovery or requirement |
| `GET` | `/api/requests/studio/requirement/{id}` | `ROLE_STUDIO` | View all outgoing candidate requests for a specific shoot requirement |
| `GET` | `/api/requests/freelancer/me` | `ROLE_FREELANCER` | Retrieve incoming work requests for authenticated freelancer |
| `GET` | `/api/requests/{id}` | Authenticated | Inspect request details and agreed terms |
| `PATCH` | `/api/requests/{id}/accept` | `ROLE_FREELANCER` | Accept studio work request |
| `PATCH` | `/api/requests/{id}/reject` | `ROLE_FREELANCER` | Reject studio work request |
| `PATCH` | `/api/requests/{id}/confirm` | `ROLE_STUDIO` | **Confirm Creator**: Confirms one chosen creator, auto-cancels competing requests, and unlocks client contacts |
| `PATCH` | `/api/requests/{id}/cancel` | Authenticated | Cancel confirmed shoot with mandatory reason |

---

## 9. Real-Time Messaging & WebSockets (`/api/chat`, `/ws`)

| Transport | Endpoint / Destination | Access | Summary |
| :--- | :--- | :--- | :--- |
| `HTTP GET` | `/api/chat/conversations` | Authenticated | List all active conversation threads for authenticated user |
| `HTTP GET` | `/api/chat/conversations/{id}` | Authenticated | Retrieve conversation metadata and participant details |
| `HTTP GET` | `/api/chat/conversations/{id}/messages` | Authenticated | Retrieve message history in chronological order |
| `HTTP PATCH` | `/api/chat/conversations/{id}/read` | Authenticated | Mark unread messages in conversation thread as read |
| `STOMP SEND` | `/app/chat.sendMessage` | Authenticated | Send real-time text message up to 4,000 characters |
| `STOMP SUB` | `/topic/conversation.{id}` | Authenticated | Live bidirectional chat subscription channel |

---

## 10. In-App Notifications (`/api/notifications`, `/ws`)

| Transport | Endpoint / Destination | Access | Summary |
| :--- | :--- | :--- | :--- |
| `HTTP GET` | `/api/notifications` | Authenticated | Retrieve paginated notifications feed |
| `HTTP GET` | `/api/notifications/unread-count` | Authenticated | Counter badge for navbar bell icon |
| `HTTP PATCH` | `/api/notifications/{id}/read` | Authenticated | Mark single notification as read |
| `HTTP PATCH` | `/api/notifications/read-all` | Authenticated | Mark all notifications as read |
| `STOMP SUB` | `/topic/notifications.{userId}` | Authenticated | Real-time push notification event subscription channel |

---

## 11. Two-Way Ratings & Reviews (`/api/ratings`)

| Method | Endpoint | Access | Summary |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/ratings` | Authenticated | Submit 1–5 star rating and review after shoot status is `COMPLETED` |
| `GET` | `/api/ratings/user/{userId}` | Public / Authenticated | Public verified review list and computed average rating |
| `GET` | `/api/ratings/requirement/{id}` | Authenticated | Inspect mutual ratings submitted for completed requirement |

---

## 12. Administrative Monitoring Platform (`/api/admin`)

| Method | Endpoint | Access | Summary |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/admin/dashboard` | `ROLE_ADMIN` | High-level platform KPIs, entity counts, and live system activity |
| `GET` | `/api/admin/studios` | `ROLE_ADMIN` | Read-only listing of all registered studios and onboarding status |
| `GET` | `/api/admin/freelancers` | `ROLE_ADMIN` | Read-only listing of all registered freelancers and profiles |
| `GET` | `/api/admin/requirements` | `ROLE_ADMIN` | Read-only audit of all shoot requirements across platform |
| `GET` | `/api/admin/requests` | `ROLE_ADMIN` | Read-only audit of all booking requests and negotiated rates |
| `GET` | `/api/admin/activity` | `ROLE_ADMIN` | Platform-wide operational activity audit stream |
| `GET` | `/api/admin/identity-submissions` | `ROLE_ADMIN` | Studio owner identity declaration verification audit |

---

## 13. Python FastAPI ML Microservice (`/ml/*` on Port 8000)

| Method | Endpoint | Access | Summary | Payload |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/predict-match` | Internal / Proxy | Single candidate match prediction | 6 normalized features in `[0.0, 1.0]` |
| `POST` | `/predict-batch` | Internal / Proxy | High-performance vectorized batch candidate prediction | Array of candidate feature sets |
| `GET` | `/model-info` | Internal / Proxy | Scikit-learn model metadata, hyperparameters, and cross-validation metrics | None |
| `GET` | `/health` | Internal / Proxy | Liveness and model loading status verification | None |
