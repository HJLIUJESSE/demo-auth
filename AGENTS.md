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
