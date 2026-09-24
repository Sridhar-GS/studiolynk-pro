# StudioLynk Agent Instructions

## Mission
Build StudioLynk as a complete, understandable final-year academic prototype for photography studios and freelancers.

## Source of truth
Before changing code:
1. Read all relevant files under `docs/`.
2. Read `PROJECT_STATUS.md`.
3. Read `REQUIREMENTS.md`.
4. Inspect the existing implementation.
5. Follow approved decisions in `docs/18-DECISIONS-AND-CONSTRAINTS.md`.

Never invent product features when the specification already defines behavior.

## Development rules
- Implement one phase at a time.
- Do not silently change architecture or technology.
- Prefer simple, maintainable solutions over unnecessary enterprise complexity.
- Do not introduce microservices, Kubernetes, Redis, Kafka, Firebase, MongoDB, payment gateways, LLM recommendations, or computer vision unless explicitly approved later.
- Use React + TypeScript + Vite + Tailwind CSS + Lucide React.
- Use Java Spring Boot + Spring Security + JWT + MySQL.
- Use Python + FastAPI + scikit-learn for ML.
- Use WebSocket/STOMP for messaging.
- Use AWS S3 for profile and portfolio images.
- Use AWS EC2 Ubuntu 24.04 LTS for deployment.
- Use Nginx only for final reverse-proxy deployment.
- Keep MySQL private to EC2.
- Never hard-code credentials or secrets.
- Never expose SMTP, JWT, AWS, or database credentials in source control.
- Validate all user input and uploaded files.
- Use consistent API error responses and HTTP status codes.
- Use meaningful names and modular classes/components.
- Add comments for non-obvious business rules and important algorithms; do not comment obvious code line-by-line.

## Phase protocol
For every phase:
1. Read the relevant documentation.
2. Inspect current code.
3. State a concise implementation plan.
4. Implement only the requested phase.
5. Build and test.
6. Fix errors found in the phase.
7. Update `PROJECT_STATUS.md`.
8. Update `REQUIREMENTS.md` status where appropriate.
9. Document important implementation decisions.
10. Report changed files, tests, known issues, and manual verification steps.
11. Stop and wait for approval before the next phase.

If a requirement is ambiguous and affects product behavior, ask before implementing. If it is only a minor implementation detail, choose a conventional simple solution and document it.

## AWS safety
Before destructive or system-wide operations, inspect the current state. Never delete the EC2 instance, S3 bucket, database data, or SSH configuration without explicit approval. Never expose MySQL port 3306 publicly.
