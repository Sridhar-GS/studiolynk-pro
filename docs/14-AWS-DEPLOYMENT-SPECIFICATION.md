# AWS Deployment Specification

## EC2
- Ubuntu 24.04 LTS
- Elastic IP
- Development via VS Code Remote SSH

## Services on EC2
- React frontend build/serving
- Spring Boot backend
- FastAPI ML service
- MySQL
- Nginx

## Ports
Development may use:
- React: 5173
- Spring Boot: 8080
- FastAPI: 8000
- MySQL: 3306

Final deployment should expose one public HTTP endpoint through Nginx.

MySQL must bind privately and must not be opened to the internet.

## Nginx
Final routing:
- `/` -> frontend
- `/api/` -> Spring Boot
- `/ml/` or internal ML route -> FastAPI as needed

WebSocket upgrade configuration must be included for STOMP messaging.

## S3
Used for:
- Studio profile/logo
- Freelancer profile
- Freelancer portfolio

## Environment variables
Examples:
- DB_URL
- DB_USERNAME
- DB_PASSWORD
- JWT_SECRET
- SMTP_HOST
- SMTP_PORT
- SMTP_USERNAME
- SMTP_PASSWORD
- AWS_REGION
- AWS_ACCESS_KEY_ID
- AWS_SECRET_ACCESS_KEY
- S3_BUCKET_NAME
- ML_SERVICE_URL

Never commit actual values.

## Security group
Public:
- 80
- 443 if HTTPS is later enabled
- 22 only from the developer's trusted IP where practical

Do not publicly expose:
- 3306
- 8000
- 8080
- 5173

## Final demo
Open:
`http://ELASTIC_IP`

No custom domain is required.
