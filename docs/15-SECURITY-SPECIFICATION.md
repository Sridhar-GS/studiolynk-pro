# Security Specification

## Authentication
- BCrypt/strong password hashing.
- JWT.
- Role-based authorization.
- Ownership checks for user resources.

## Password reset
- Secure OTP generation.
- OTP expiry.
- Attempt limits.
- Resend cooldown.
- Do not log OTP.

## Identity documents
Prototype only.
- Do not implement external Aadhaar verification.
- Do not store Aadhaar number.
- Do not expose identity document publicly.
- Restrict identity submission access.
- Use dummy/test documents for demonstrations.

## API
- Validate DTOs.
- Validate ownership.
- Return appropriate HTTP status.
- Consistent error response.

## File upload
- Validate MIME type.
- Validate extension.
- Prevent path traversal.
- Generate safe S3 object names.
- Check user ownership.

## Database
- Parameterized queries through ORM/repository layer.
- Foreign keys.
- Transactions for confirmation and booking conflict logic.

## Secrets
Use environment variables.
Never commit `.env`.

## Logging
Do not log:
- passwords
- JWTs
- OTPs
- SMTP credentials
- AWS credentials
- sensitive private event-person information

## AWS
MySQL must not be publicly reachable.
