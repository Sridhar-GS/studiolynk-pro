# Database Specification

Use normalized MySQL tables. Exact naming may be adjusted consistently by the backend implementation.

## Core identity
- users
- roles or role enum
- refresh/session records only if implementation needs them
- password_reset_otps

## Studio
- studios
- studio_social_links
- studio_identity_submissions

## Freelancer
- freelancers
- skills
- freelancer_skills
- services
- freelancer_services
- equipment_categories
- equipment
- freelancer_equipment

## Portfolio
- portfolios
- portfolio_categories
- portfolio_images

## Availability
- freelancer_availability
- availability_time_slots if multiple slots are implemented

## Work
- work_requirements
- work_requirement_skills
- work_requirement_services
- work_requirement_equipment
- work_requests
- work_confirmation/private detail fields

## Communication
- conversations
- conversation_participants
- messages
- message_read_state if required

## Notifications
- notifications

## Ratings
- ratings

## Design principles
- Use foreign keys.
- Use indexes on frequent lookup columns.
- Use timestamps.
- Use enum/status values consistently.
- Never store passwords in plaintext.
- Store S3 object keys/URLs, not image binary data.
- Keep private event-person information in protected columns/relations and never return it before confirmation.

## Important relationships
User 1:1 Studio OR Freelancer.
Freelancer 1:1 Portfolio.
Portfolio 1:N Categories.
Category 1:N Images.
Freelancer 1:N Availability.
Studio 1:N WorkRequirements.
WorkRequirement 1:N WorkRequests.
WorkRequirement 1:1 confirmed freelancer for the prototype.
WorkRequirement 1:N Messages through a requirement-specific conversation.
Completed WorkRequirement 1:N Ratings, with one Studio->Freelancer and one Freelancer->Studio rating maximum.
