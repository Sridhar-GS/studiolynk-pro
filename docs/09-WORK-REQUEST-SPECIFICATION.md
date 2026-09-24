# Work Request Specification

## Requirement
Studio creates a work requirement.

Fields:
- Event name
- Event type
- Date
- Start time
- End time
- Location/address
- Coordinates if selected
- Services
- Skills
- Equipment needs
- Budget
- Description
- Private event-person details

## Discovery
Studio may:
1. Search freelancers directly.
2. Create a requirement and use AI matching.

## Request
Studio selects freelancer -> sends request.

Freelancer sees:
- event/work type
- date/time
- location
- required services/skills
- budget
- description

Freelancer does not initially see:
- private event-person name
- private event-person phone
- other protected contact information

## Negotiation
Messaging is available after request creation.
Budget/commission negotiation occurs in chat.

## Decision
Freelancer accepts or rejects.
Studio reviews accepted freelancer and confirms one.

After Studio confirmation:
- request becomes confirmed
- private event-person details are revealed to the confirmed freelancer
- other pending requests for the requirement close
- final agreed price is stored

## Cancellation
Both sides may cancel confirmed work.
Cancellation requires a reason.

## Completion
Confirmed -> In Progress -> Completed.
Completed work enables two-way ratings.
