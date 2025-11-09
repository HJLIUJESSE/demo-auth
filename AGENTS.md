# Repository Guidelines

## Project Structure & Module Organization
The Spring Boot backend lives in `src/main/java/com/example/demo_auth`; group related code under subpackages such as `config`, `controller`, and `service`. Runtime configuration, templates, and static files belong in `src/main/resources`, while integration tests should mirror the package layout in `src/test/java`. The React + Vite client sits in `frontend/`, with page components in `frontend/src/components` and API helpers in `frontend/src/services`.

## Build, Test, and Development Commands
- `./mvnw spring-boot:run` — start the backend with live reload.
- `./mvnw clean package` — compile, test, and produce the runnable JAR in `target/`.
- `./mvnw test` — execute the JUnit test suite.
- `npm install` (run once inside `frontend/`) — install client dependencies.
- `npm run dev` — launch the Vite dev server.
- `npm run build` — generate optimized frontend assets under `frontend/dist`.
- `npm run lint` — apply the shared ESLint/TypeScript checks.

## Coding Style & Naming Conventions
Target Java 17 with 4-space indentation. Keep classes in `PascalCase`, methods and fields in `lowerCamelCase`, and prefer Lombok annotations where already present to avoid boilerplate. For React, author `PascalCase` components, prefix reusable hooks with `use`, and colocate CSS next to the component that owns it. Run `npm run lint -- --fix` before committing frontend changes.

## Testing Guidelines
Use JUnit 5 and Spring Boot test utilities; name new classes `*Tests` and scope them to the package they cover. Reach for slice tests (`@WebMvcTest`, `@DataJpaTest`) before `@SpringBootTest` to keep suites fast. The frontend currently has no automated tests; when adding them, place Vitest or Playwright specs under `frontend/src/__tests__` and wire the command into `package.json`.

## Commit & Pull Request Guidelines
History so far favors short descriptive subjects; continue with imperative summaries capped at 72 characters (e.g., `backend: add jwt filter`). Reference issue IDs or Jira keys where relevant, and record config or migration steps in the body. Pull requests must explain the change, list backend/frontend checks run (`./mvnw test`, `npm run lint`, etc.), and attach UI screenshots or API samples when behavior changes. Update docs when adding endpoints, properties, or required environment variables.

## Security & Configuration Tips
Do not commit credentials; keep secrets in environment variables or `.env.local` (already `.gitignore`d). Document new required properties inside `application.yml` comments. For JWT or database updates, rotate keys locally and describe the rollout plan in the PR.

## Prerequisites
- Java 17 (set `JAVA_HOME` accordingly)
- Maven Wrapper (`mvnw`/`mvnw.cmd` in repo)
- Node.js 18+ and npm 9+

## Dev Ports
- Backend: `http://localhost:8080`
- Frontend: `http://localhost:5173`

## Windows Command Variants
- `mvnw.cmd spring-boot:run`
- `mvnw.cmd clean package`
- `mvnw.cmd test`

## Local Env & Configuration
- Backend configuration lives in `src/main/resources/application.yml` and can be overridden via environment variables.
  - Common properties (example names; document specifics where introduced):
    - `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password`
    - `jwt.secret`, `jwt.expiration`
  - Equivalent env vars: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION`.
- Frontend env variables go in `frontend/.env.local` (gitignored):
  - `VITE_API_BASE_URL` (e.g., `http://localhost:8080` or `/api` when using a dev proxy)

## Dev Proxy & CORS
- Prefer a Vite dev proxy to avoid CORS during development. In `frontend/vite.config.ts`, target the backend and rewrite to `/api`:
  
  ```ts
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
    },
  }
  ```
- Point frontend API calls to `"/api"` or set `VITE_API_BASE_URL=/api` locally.

## Troubleshooting
- Maven wrapper permissions on Unix: `chmod +x mvnw`.
- Port conflicts: change backend port via `server.port` in `application.yml` or `SERVER_PORT` env var; adjust Vite proxy accordingly.
- Dependency mismatches: ensure Node and Java versions match the prerequisites above.
