# Authentication Specification

## Registration
1. User submits email/password.
2. Account is created.
3. User chooses Studio or Freelancer as the first onboarding step.
4. Role-specific onboarding starts.
5. Incomplete account cannot access the main application.
6. Completed onboarding redirects to profile.

## Login
Single page:
- Email
- Password
- Login
- Forgot password

Role determines destination:
- Studio -> Studio dashboard
- Freelancer -> Freelancer dashboard
- Admin -> Admin dashboard

## Password reset
1. Enter email.
2. Generate secure 6-digit OTP.
3. Store hashed OTP or otherwise securely protected OTP record.
4. Send via configured SMTP.
5. OTP expires after 5 minutes.
6. Limit attempts.
7. Resend cooldown.
8. Verify OTP.
9. Set new password.

## Password
Minimum:
- 8 characters
- uppercase
- lowercase
- number
- special character

## JWT
Use Spring Security.
Backend must authorize by role and ownership.

## Security rules
- Never trust role sent by frontend.
- Never expose password hash.
- Do not log OTP/password/token.
- Secrets through environment variables.
