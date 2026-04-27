# Spring Batch Dashboard

A web dashboard for inspecting Spring Batch metadata (job runs, step executions, throughput, status distributions) across multiple PostgreSQL environments.

## What's in here

| Component | Stack | Purpose |
|---|---|---|
| [`backend/`](backend/) | Spring Boot 4, Java 21, Spring Data JPA, OAuth2 (GitHub) | REST API that reads `BATCH_*` metadata and serves it to the frontend. Multi-environment via per-request datasource routing. |
| [`frontend/`](frontend/) | React 19, Vite, MUI, TanStack Query, Vitest | The dashboard SPA. Browses jobs, runs, and per-execution step details. |

The components don't share code — they're independent apps that meet at the database.

## Screenshots

### Login
![Login](docs/login.png)

### Overview
![Overview](docs/overview.png)

### Job Details
![Job Details](docs/job-details.png)

### Job Execution
![Job Execution](docs/job-execution.png)

## Quick start

You'll need: JDK 21, Node 20+, Yarn 4 (Berry), Docker.

```bash
# 1. Backend — pulls up Postgres in a docker container, serves on :8080
cd backend
cp .env.example .env                      # add GITHUB_CLIENT_ID / GITHUB_CLIENT_SECRET
./mvnw spring-boot:run

# 2. Frontend — serves on :5173
cd ../frontend
yarn install
yarn dev
```

Open `http://localhost:5173` and log in with GitHub. The backend's interactive API docs are at [`http://localhost:8080/swagger-ui/index.html`](http://localhost:8080/swagger-ui/index.html).

To run the dashboard without configuring OAuth or a database, set `VITE_USE_MOCK_DATA=true` in `frontend/.env` — every API endpoint serves canned data instead.

## Multi-environment

The dashboard supports browsing multiple Postgres databases, switched via the environment selector in the sidebar. The selection is forwarded to the backend on every request as the `X-Environment` header, and the backend routes to the matching datasource at `app.datasources[*]` in [`backend/src/main/resources/application.yml`](backend/src/main/resources/application.yml).

To add a new environment, append an entry to that list and restart. The selector picks it up via `GET /api/environments`.

## Architecture

```
                   X-Environment header
   ┌─────────┐    ┌─────────────────┐
   │frontend │ ──▶│    backend      │
   │ (React) │    │  (Spring Boot)  │
   └─────────┘    └────────┬────────┘
                           │ JPA / JdbcTemplate
                           ▼
                  ┌──────────────────┐
                  │   Postgres(es)   │
                  └──────────────────┘
```

- **Backend** never writes — it's read-only over the BATCH_* schema.
- **Frontend** persists the chosen environment to `localStorage` and forwards it on every request.

## Documentation

Each component has its own conventions doc:

- [AGENTS.md](AGENTS.md) — repo overview, runbook, cross-cutting conventions.
- [backend/AGENTS.md](backend/AGENTS.md) — controller/service/repository patterns, dynamic datasource routing, MapStruct setup, error handling.
- [frontend/AGENTS.md](frontend/AGENTS.md) — page/tile container conventions, shared component inventory, query-hook pattern, alias setup.

## Tooling notes

- Backend uses Maven via the wrapper (`./mvnw`); never `mvn` directly.
- Frontend uses Yarn 4 (`.yarn/` is committed via PnP-less node-modules install).
- Tests: `./mvnw test` (backend), `yarn test` (frontend).
- Imports in the frontend use the `~/` alias to `src/`; siblings stay relative.
- Backend errors never leak SQL or class names to clients (see [GlobalExceptionHandler](backend/src/main/java/com/guavasoft/springbatch/dashboard/config/GlobalExceptionHandler.java)).
