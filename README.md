# Spring Batch Dashboard

A web dashboard for inspecting Spring Batch metadata (job runs, step executions, throughput, status distributions) across multiple PostgreSQL **or** MySQL environments.

## What's in here

| Component | Stack | Purpose |
|---|---|---|
| [`backend/`](backend/) | Spring Boot 4, Java 21, Spring Data JPA, OAuth2 | REST API that reads `BATCH_*` metadata and serves it to the frontend. Multi-environment via per-request datasource routing; supports Postgres or MySQL (one engine per boot, picked via Maven profile). |
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
# 1. Backend — pulls up Postgres + MySQL in docker containers, serves on :8080
cd backend
cp .env.example .env                      # add GITHUB_CLIENT_ID / GITHUB_CLIENT_SECRET (and DB creds)

./mvnw spring-boot:run                    # Postgres (default)
# or:
./mvnw -Pmysql spring-boot:run            # MySQL

# 2. Frontend — serves on :5173
cd ../frontend
yarn install
yarn dev
```

Open `http://localhost:5173` and log in. The backend's interactive API docs are at [`http://localhost:8080/swagger-ui/index.html`](http://localhost:8080/swagger-ui/index.html).

To run the dashboard without configuring OAuth or a database, set `VITE_USE_MOCK_DATA=true` in `frontend/.env` — every API endpoint serves canned data instead.

## Choosing the database engine

The Maven profile is the single switch. It bundles the right JDBC driver, sets `app.dialect`, and activates the matching local config:

| | Postgres (default) | MySQL |
|---|---|---|
| Build / run | `./mvnw …` | `./mvnw -Pmysql …` |
| Active config | [`application-local-postgresql.yml`](backend/src/main/resources/application-local-postgresql.yml) | [`application-local-mysql.yml`](backend/src/main/resources/application-local-mysql.yml) |
| Driver | `org.postgresql:postgresql` | `com.mysql:mysql-connector-j` |

Mixing engines in one boot is not supported — every entry under `app.datasources` must match the active engine. Engine-specific SQL (epoch math, `NULLS LAST`) is routed through the [`SqlDialect`](backend/src/main/java/com/guavasoft/springbatch/dashboard/dialect/SqlDialect.java) strategy so repository code stays portable.

## Multi-environment

The dashboard supports browsing multiple databases of the active engine, switched via the environment selector in the sidebar. The selection is forwarded to the backend on every request as the `X-Environment` header, and the backend routes to the matching datasource at `app.datasources[*]` in the active local-profile YAML.

To add a new environment, append an entry to that list (matching the active engine) and restart. The selector picks it up via `GET /api/environments`.

## Authentication

OAuth2 via Spring Security; defaults wire up GitHub but any provider works by remapping attribute names under `app.auth.attributes.*` (e.g. for Google: `login=email`, `avatar-url=picture`). An optional comma-delimited `app.auth.allowed-logins` allow-list rejects logins outside the list at OAuth2 user-loading time.

## Architecture

```mermaid
flowchart LR
    User((User))
    Frontend["Frontend<br/>React + Vite"]
    Backend["Backend<br/>Spring Boot"]
    OAuth2["OAuth2 Provider<br/>(GitHub default)"]
    DB[("Postgres(es) or MySQL(s)<br/>one engine per boot")]

    User -->|browser| Frontend
    Frontend -->|"REST + X-Environment header<br/>(JSESSIONID cookie)"| Backend
    Frontend -.->|"login redirect"| OAuth2
    OAuth2 -.->|"/login/oauth2/code/* callback"| Backend
    Backend -->|"JPA / JdbcTemplate"| DB
```

- **Backend** never writes to the BATCH_* schema — read-only.
- **Frontend** persists the chosen environment to `localStorage` and forwards it on every request as `X-Environment`.
- **OAuth2** flow: the frontend opens the provider login; the provider posts back to the backend's callback; the backend establishes a session (`JSESSIONID`) and redirects to `app.oauth2.success-url`. Subsequent API calls authenticate via the cookie. The provider is configurable via Spring Security; attribute-name mapping and an optional `app.auth.allowed-logins` allow-list make it provider-agnostic (see [Authentication](#authentication)).

## Documentation

Each component has its own conventions doc:

- [AGENTS.md](AGENTS.md) — repo overview, runbook, cross-cutting conventions.
- [backend/AGENTS.md](backend/AGENTS.md) — controller/service/repository patterns, engine selection, dialect strategy, dynamic datasource routing, MapStruct setup, error handling.
- [frontend/AGENTS.md](frontend/AGENTS.md) — page/tile container conventions, shared component inventory, query-hook pattern, alias setup.

## Tooling notes

- Backend uses Maven via the wrapper (`./mvnw`); never `mvn` directly.
- Frontend uses Yarn 4 (`.yarn/` is committed via PnP-less node-modules install).
- Tests: `./mvnw test` (Postgres) or `./mvnw -Pmysql test` (MySQL); `yarn test` (frontend). CI runs both engines as a matrix.
- Imports in the frontend use the `~/` alias to `src/`; siblings stay relative.
- Backend errors never leak SQL or class names to clients (see [GlobalExceptionHandler](backend/src/main/java/com/guavasoft/springbatch/dashboard/config/GlobalExceptionHandler.java)).
