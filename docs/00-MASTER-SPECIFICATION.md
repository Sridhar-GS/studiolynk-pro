# StudioLynk — Master Specification

## 1. Project identity

**Project:** StudioLynk

**Academic positioning:** Final-year engineering project prototype.

**Core idea:** A dedicated collaboration platform for photography studios and freelancers. Studios can discover freelancers, inspect their professional information and portfolios, check availability, create work requirements, communicate with freelancers, and select suitable freelancers using an AI-based matching system.

This is not a client-facing marketplace. There is no Client role.

## 2. Product goals

1. Centralize Studio–Freelancer collaboration.
2. Give freelancers a structured professional identity and portfolio.
3. Let studios search using practical filters.
4. Let studios create detailed future work requirements.
5. Provide availability-aware discovery.
6. Add a clearly demonstrable ML component.
7. Keep the application simple enough for a final-year team to build, understand, test, and demonstrate.

## 3. Roles

### Studio
A photography studio that searches for and collaborates with freelancers.

### Freelancer
A photographer/videographer/editor or related professional who maintains a professional profile, portfolio, equipment, pricing, availability, and receives studio work requests.

### Admin
A monitoring role. Admin has a dashboard and can view system data. Admin does not generally edit/delete user data and does not approve Studio/Freelancer access.

## 4. Technology

### Frontend
- React
- TypeScript
- Vite
- Tailwind CSS
- Lucide React

### Backend
- Java
- Spring Boot
- Spring Security
- JWT
- WebSocket/STOMP
- REST APIs

### Database
- MySQL on EC2

### ML
- Python
- FastAPI
- pandas
- NumPy
- scikit-learn
- Decision Tree Regressor

### AWS
- EC2 Ubuntu 24.04 LTS
- S3
- Elastic IP
- Nginx for final reverse proxy

### Development
VS Code Remote SSH into EC2.

## 5. High-level architecture

```text
Browser
   |
   | HTTPS/HTTP for prototype
   v
Nginx
   |
   +--> React frontend
   |
   +--> /api/* --> Spring Boot
   |                 |
   |                 +--> MySQL
   |                 +--> S3
   |                 +--> FastAPI ML
   |
   +--> /ml/* --> FastAPI
```

During development, services may run on separate local ports. Nginx is used for the final demo so staff can open one Elastic IP.

## 6. Authentication

- Single login page.
- Signup begins with account creation, then role selection as the first onboarding step.
- Role-specific onboarding follows.
- Only onboarding-completed accounts may enter the main application.
- Email + password.
- JWT + Spring Security.
- Password reset through email OTP using a free SMTP solution such as Gmail SMTP with an App Password.
- No Firebase.
- OTP: 6 digits, 5-minute validity, resend cooldown, limited attempts.
- Password: minimum 8 characters with uppercase, lowercase, number, and special character.

## 7. Studio onboarding

Collect:
- Studio name
- Studio logo/photo
- Owner name
- Email
- Phone
- Address
- Location selection
- Years of operation
- Instagram/website/social links
- Prototype owner identity-document submission/declaration

Location behavior:
- User enters address.
- User can click a map link to open Google Maps.
- Store address text and coordinates when available.
- No Google Maps API is required for the prototype.

Identity workflow:
- Do not implement external Aadhaar verification.
- Do not store an Aadhaar number.
- Treat it as a prototype identity-document/declaration workflow.
- Studio can use the platform after onboarding without admin approval.

## 8. Freelancer onboarding

Collect:
- Name
- Profile photo
- Email
- Phone
- Address
- Location
- Experience
- Bio
- Skills
- Services
- Equipment
- Full-day charge
- Half-day charge

Skills and services:
- Predefined choices.
- User can add custom choices.

Equipment:
- Groups such as Camera, Lens, Lighting, Software, Other.
- Predefined choices plus custom equipment.

## 9. Freelancer portfolio

One freelancer has one portfolio.

Structure:
```text
Freelancer
  -> Portfolio
     -> Category
        -> Multiple Images
```

Features:
- Add category.
- Upload multiple images.
- Edit.
- Delete.
- Drag/drop reorder.
- No public/private toggle.
- S3 storage.
- Portfolio relevance uses category and metadata/text; no computer vision.

## 10. Availability

Rolling ten-day window.

Each day:
- Available
- Busy
- Not Set

Available day:
- Start time
- End time

Studio search within the next ten days:
- Busy and Not Set are hidden from available results.
- Requested date/time must overlap the freelancer's available time.
- Availability is a hard filter before ML ranking.

Beyond ten days:
- Freelancer can appear if other filters match.
- Display availability as unknown/not set.
- Do not claim they are available.

Confirmed work:
- Prevent overlapping confirmed bookings.
- Make the relevant time unavailable/busy.

## 11. Studio discovery

Two modes:

### Normal search
Filters:
- Service
- Skill
- Location
- Date
- Time
- Full/Half Day
- Budget
- Experience
- Equipment

### AI requirement search
Studio creates a work requirement. The system:
1. Applies hard constraints.
2. Finds eligible freelancers.
3. Sends features to the ML service.
4. Receives match scores.
5. Sorts by descending match score.

Search result card should show:
- Profile photo
- Name
- Location/distance
- Public rating
- Experience
- Skills/services
- Availability
- Pricing
- AI match score where applicable
- View Profile
- Send Request

## 12. Work requirements

A Studio can create a detailed requirement/event card.

Fields:
- Event name
- Event type
- Date
- Start time
- End time
- Event location
- Required services
- Required skills
- Equipment needs
- Budget
- Description
- Private event-person details such as name/phone when required

Status:
- Draft
- Open
- Requested
- Accepted
- Confirmed
- In Progress
- Completed
- Cancelled

Multiple freelancers can be requested for the same requirement, but the prototype ultimately confirms one freelancer.

## 13. Privacy and request workflow

Initial freelancer view:
- Event/work type
- Date
- Time
- Location
- Required services/skills
- Budget
- Description

Do not expose private event-person contact information initially.

Workflow:
```text
Studio creates requirement
  -> searches
  -> sends request
  -> freelancer sees limited information
  -> freelancer can message
  -> freelancer accepts/rejects
  -> studio reviews
  -> studio confirms one freelancer
  -> private event-person details become visible
  -> work becomes confirmed
```

Negotiation occurs in messaging.

Final agreed price is stored after confirmation.

Both Studio and Freelancer can cancel a confirmed work with a reason.

## 14. Messaging

- Requirement-specific conversation.
- Studio <-> Freelancer.
- WebSocket/STOMP.
- Text only.
- Timestamp.
- Read/unread.
- Messaging is available after request is sent, before acceptance, for negotiation.
- No email messaging notifications.

## 15. Notifications

In-app only.

Events include:
- New request
- Request accepted/rejected
- Studio confirmation
- New message
- Work lifecycle changes
- Work reminders
- Rating reminders

Notifications have read/unread state and mark-all-read.

## 16. Ratings

After work is completed:
- Studio can rate Freelancer.
- Freelancer can rate Studio.
- Public profile display.
- Star score + optional review.
- One rating per direction per completed work.
- Ratings do not affect ML ranking.

## 17. ML design

Goal: predict a freelancer match score from 0–100.

Model:
**DecisionTreeRegressor**

Features:
- Skill match
- Portfolio relevance
- Experience
- Budget compatibility
- Location distance
- Availability/time compatibility

No ratings.

Dataset:
- Approximately 5,000 synthetic records.
- Labels/scores generated through explicit logical rules that represent plausible business relationships.
- Do not create arbitrary random labels.

Training:
- 80/20 train/test split.
- 5-fold cross-validation on training data.
- Tune parameters such as max_depth, min_samples_split, and min_samples_leaf.
- Evaluate with MAE, RMSE, R².
- Inspect training vs validation/test performance to control overfitting and underfitting.

Runtime:
- Spring Boot calls FastAPI.
- FastAPI returns match score.
- Spring Boot sorts eligible freelancers.

Output example:
`92.4% Match`

## 18. Admin

Admin login:
- Email + password.

Admin dashboard:
- Total Studios
- Total Freelancers
- Active users
- Studio identity-document/declaration monitoring
- Requirements
- Requests
- Ratings/activity summaries

Admin is monitoring/view-only. No general edit/delete. No approval gate for Studio/Freelancer platform access.

## 19. UI/UX

General:
- Responsive React application.
- Tailwind CSS.
- Lucide React icons.
- Visual cards/icons/images for important input categories.
- Strong freelancer profile presentation.
- Accessible labels and validation.
- Loading, error, empty, success states.

Desktop:
- Top navigation.

Mobile:
- Bottom navigation.

Studio navigation:
- Dashboard
- Find Freelancers
- Requirements
- Requests
- Messages
- Notifications
- Profile

Freelancer navigation:
- Dashboard
- Work Requests
- Availability
- Portfolio
- Messages
- Notifications
- Profile

Admin:
- Dashboard
- Studios
- Freelancers
- Requirements
- Requests
- Activity/monitoring

## 20. S3

Logical prefixes:
- `studio-profiles/`
- `freelancer-profiles/`
- `portfolio/`

Prototype may use public object URLs. Validate uploads and store only S3 object metadata/URL in MySQL.

## 21. Security

- JWT access tokens.
- Password hashes, never plaintext.
- Role-based authorization.
- MySQL private.
- Secrets via environment variables.
- Validate request ownership.
- Validate file type/size where practical.
- Never trust client-side role claims.
- Backend enforces all business rules.
- Do not log passwords, OTPs, tokens, or sensitive identity-document contents.

## 22. Academic maintainability

Important code should be understandable to reviewers.

Comments should explain:
- Availability rules
- Booking conflict logic
- Request confirmation
- Private-data release
- ML feature preparation
- S3 integration
- WebSocket behavior
- Authentication decisions

Avoid comments on obvious getters, setters, or simple JSX.

## 23. Non-goals

Do not implement:
- Client accounts
- Payment gateway
- Subscription billing
- Firebase
- MongoDB
- RDS
- LLM recommendation
- Computer vision
- Kubernetes
- Kafka
- Redis unless later approved
- Complex microservices
- Production-grade multi-region infrastructure

## 24. Definition of complete

The project is complete only when:
- All approved requirements are implemented.
- Frontend builds.
- Backend builds.
- ML service runs.
- MySQL schema initializes correctly.
- S3 upload works.
- Authentication works.
- Main workflows work end-to-end.
- WebSocket messaging works.
- ML prediction works through Spring Boot.
- Relevant tests pass.
- EC2 deployment works.
- Nginx exposes the final application through Elastic IP.
- Documentation is updated.
