# Demo Auth Setup Guide

This repository delivers a Spring Boot 3.5 backend and a React 19 + Vite frontend that together implement a basic authentication flow. Follow the steps below to install every dependency, configure MySQL, and start both applications locally.

## Prerequisites
- Java 17 (Temurin, Corretto, or another distribution with TLS support)
- Maven Wrapper (`mvnw` is included; a global Maven install is optional)
- Node.js 20 LTS and npm (nvm is recommended for version management)
- MySQL 8.0 or later
- Git 2.40 or later
- Optional: Docker + Docker Compose if you prefer a containerized MySQL instance

## Clone the Repository
```bash
git clone https://github.com/<your-org>/demo-auth.git
cd demo-auth
```

## Configure MySQL
### Option A: Local MySQL Service
Connect as a privileged user and create the database and credentials:
```sql
CREATE DATABASE demo_auth CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'demo_user'@'localhost' IDENTIFIED BY 'StrongPassword123!';
GRANT ALL PRIVILEGES ON demo_auth.* TO 'demo_user'@'localhost';
FLUSH PRIVILEGES;
```

### Option B: Docker Container
```bash
docker run --name demo-auth-mysql \
  -e MYSQL_ROOT_PASSWORD=changeme \
  -e MYSQL_DATABASE=demo_auth \
  -e MYSQL_USER=demo_user \
  -e MYSQL_PASSWORD=StrongPassword123! \
  -p 3306:3306 -d mysql:8.0
```

### Apply Application Settings
Edit `src/main/resources/application.yml` to match your credentials or override them with environment variables:
```bash
export SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/demo_auth?useSSL=false&serverTimezone=Asia/Taipei"
export SPRING_DATASOURCE_USERNAME=demo_user
export SPRING_DATASOURCE_PASSWORD=StrongPassword123!
export JWT_SECRET=$(openssl rand -base64 64)
```
Replace `JWT_SECRET` with a random string of at least 64 characters. For production, change `spring.jpa.hibernate.ddl-auto` to `validate` and manage schema changes with Flyway or Liquibase.

## Run the Spring Boot Backend
```bash
./mvnw spring-boot:run
```
The first launch downloads dependencies and exposes REST endpoints at `http://localhost:8080`. Useful checks:
- Package a runnable JAR: `./mvnw clean package` (outputs to `target/`).
- Execute the test suite: `./mvnw test`.

## Run the React + Vite Frontend
```bash
cd frontend
npm install
npm run dev
```
The dev server listens on `http://localhost:5173` and proxies API traffic to the backend. Before pushing changes, run `npm run lint`. Build production assets with `npm run build`; output files appear in `frontend/dist`.

## Verify the Stack
1. With both servers running, open `http://localhost:5173` and exercise the registration and login screens.
2. Use an HTTP client such as Postman or `curl` to hit the backend directly:
   ```bash
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"demo","password":"demo"}'
   ```
3. Never commit secrets or certificates. Keep JWT keys and database credentials in environment variables or a secrets manager.

## Deployment Tips
- Prefer managed MySQL services (AWS RDS, Azure Database for MySQL, etc.) and restrict network access to trusted apps.
- Add CI steps (GitHub Actions, GitLab CI, …) that run `./mvnw test` and `npm run lint` before merging.
- Tighten CORS (`WebConfig.allowedOrigins`) for production and serve both apps behind HTTPS.
