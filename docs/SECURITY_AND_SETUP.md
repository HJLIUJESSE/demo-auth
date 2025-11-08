Security and Local Setup

Overview
- Secrets do not go into the repo. Use environment variables or files outside the project.
- Cloud SQL Proxy should be started with a credentials file path outside the repo.
- Backend reads DB and JWT secrets from env vars only.

Environment Variables
- SPRING_DATASOURCE_URL: jdbc URL (default points to 127.0.0.1:3307/zapp_demo_db)
- SPRING_DATASOURCE_USERNAME: DB user (recommend non-root)
- SPRING_DATASOURCE_PASSWORD: DB password (required)
- JWT_SECRET: at least 64 characters (required)
- CORS_ALLOWED_ORIGINS: frontend origins, comma-separated (default http://localhost:5173)

PowerShell (Windows) example
```
$env:SPRING_DATASOURCE_URL = "jdbc:mysql://127.0.0.1:3307/zapp_demo_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
$env:SPRING_DATASOURCE_USERNAME = "app_user"
$env:SPRING_DATASOURCE_PASSWORD = "<your-password>"
$env:JWT_SECRET = "<64+ char random string>"
$env:CORS_ALLOWED_ORIGINS = "https://your-frontend.example,https://www.your-frontend.example"
.
\mvnw.cmd spring-boot:run
```

Cloud SQL Proxy (Windows) example
```
.
\cloud-sql-proxy.exe <PROJECT:REGION:INSTANCE> --credentials-file "D:\\secrets\\cloudsql\\key.json" --address 127.0.0.1 --port 3307 --verbose
```
- Store key.json outside the repo (e.g. D:\secrets\cloudsql\key.json). The repo .gitignore includes key.json.

CORS Configuration
- Set CORS_ALLOWED_ORIGINS to your domain(s). Multiple origins are supported via comma.
- Example: CORS_ALLOWED_ORIGINS="https://app.example.com,https://www.app.example.com"

Production Notes
- Provide env vars via systemd EnvironmentFile, Windows service (NSSM) environment, Docker/K8s secrets, or Cloud Secret Manager.
- Disable show-sql and manage schema via migrations in production.
