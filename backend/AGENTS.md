# Backend agent guide

Spring Boot 4 service that exposes the Spring Batch metadata as a REST API for the dashboard frontend. Reads from one or more PostgreSQL databases that already host `BATCH_*` metadata tables (the schema Spring Batch creates for itself). Doesn't write to those tables.

## Stack

- Java 21, Spring Boot 4.0.x, Spring Framework 7
- Spring MVC (`spring-boot-starter-webmvc`)
- Spring Data JPA + Hibernate 6 for entity-based reads (`BATCH_JOB_INSTANCE`, `BATCH_JOB_EXECUTION`, `BATCH_STEP_EXECUTION`, etc.)
- `NamedParameterJdbcTemplate` for any read where dynamic `ORDER BY` / aggregates are needed (Spring Data's `Sort` rewrites property paths in unsafe ways for native queries — see [Repository conventions](#repository-conventions))
- MapStruct for DTO mapping (Lombok + MapStruct annotation processors wired in `pom.xml`)
- Spring Security OAuth2 client (GitHub login) — session-cookie auth, frontend gets a `JSESSIONID`
- SpringDoc OpenAPI — interactive UI at [`http://localhost:8080/swagger-ui/index.html`](http://localhost:8080/swagger-ui/index.html), raw spec at `/v3/api-docs`
- Testcontainers + Spring Boot Testcontainers for integration tests
- Build: Maven wrapper (`./mvnw`)

## Layout

```
src/main/java/com/guavasoft/springbatch/
  dashboard/
    DashboardApplication.java   @SpringBootApplication entry point
    config/
      SecurityConfig.java                  SecurityFilterChain (OAuth2 login + permitAll on /api/auth/me)
      DatasourcesProperties.java           binds app.datasources from application.yml
      DynamicDataSourceConfig.java         AbstractRoutingDataSource over the configured pool
      DataSourceContext.java               ThreadLocal lookup-key holder
      GlobalExceptionHandler.java          @RestControllerAdvice; never leaks SQL/stack traces
      DataSourceContextFilter.java         reads X-Environment request header → DataSourceContext
    controller/                            REST endpoints — AuthController (/api/auth/me) + dashboard endpoints
    service/                               business logic
    repository/                            Spring Data JPA + custom JdbcTemplate fragments
    entity/                                JPA entities mapped onto BATCH_* tables (read-only)
    entity/projection/                     interface-based Spring Data projections
    mapper/                                MapStruct mappers (entity/projection → model record)
    model/                                 immutable response records (formerly `dto/`)
db/init/                                   Postgres docker compose initdb scripts
```

## Multi-environment / dynamic datasource

Each frontend request includes an `X-Environment: <name>` header (set by the axios interceptor based on the user's selection). The backend wires this through:

1. [DataSourceContextFilter](src/main/java/com/guavasoft/springbatch/dashboard/config/DataSourceContextFilter.java) — `OncePerRequestFilter` that copies the header into a ThreadLocal `DataSourceContext`.
2. [DynamicDataSourceConfig](src/main/java/com/guavasoft/springbatch/dashboard/config/DynamicDataSourceConfig.java) — `AbstractRoutingDataSource` whose `determineCurrentLookupKey()` reads `DataSourceContext.get()`.
3. Available environments come from `app.datasources` in [application.yml](src/main/resources/application.yml) (name, url, username, password). The first entry is the default when no header / unknown header is supplied.

Adding a new environment: add another item under `app.datasources` and restart. The frontend's `EnvironmentSelector` will pick it up automatically from `GET /api/environments`.

## Controller / service / repository pattern

- **Controller** ([dashboard/controller/](src/main/java/com/guavasoft/springbatch/dashboard/controller/)) — thin: `@RequestMapping`, `@PathVariable` / `@RequestParam` validation, `@Operation` for OpenAPI, calls service. Use `@Validated` on the class to enable Bean Validation on parameters; pair with `@Min`, `@Max`, `@NotBlank`, etc.
- **Service** ([dashboard/service/](src/main/java/com/guavasoft/springbatch/dashboard/service/)) — `@Service` + `@Transactional(readOnly = true)`. Coordinates repositories and mappers; never returns entities directly.
- **Repository** — see below.

## Repository conventions

Two repository styles in use; pick based on shape of the query:

### Spring Data JPA repository

Use for straightforward reads against an entity, including derived queries and named JPQL/native `@Query` with **fixed** ordering. Example: [JobInstanceRepository](src/main/java/com/guavasoft/springbatch/dashboard/repository/JobInstanceRepository.java).

### `JdbcTemplate`-backed custom fragment

Use when you need any of: dynamic ORDER BY, dynamic LIMIT/OFFSET pagination, aggregate ordering, or hand-rolled SQL projection that doesn't map cleanly to an entity / interface projection.

Pattern (see [JobExecutionRepositoryCustom](src/main/java/com/guavasoft/springbatch/dashboard/repository/JobExecutionRepositoryCustom.java) + [JobExecutionRepositoryCustomImpl](src/main/java/com/guavasoft/springbatch/dashboard/repository/JobExecutionRepositoryCustomImpl.java)):

1. Declare a `*Custom` interface with the dynamic methods.
2. Have the JPA repository extend both `JpaRepository<E, ID>` and the custom interface — Spring Data wires the impl automatically.
3. Implement the custom interface as a `@Repository` class taking `NamedParameterJdbcTemplate` via constructor.
4. Whitelist allowed sort fields → SQL expression in a `Map<String, String>` *inside the impl*, never accept raw SQL from the controller.
5. Build the `ORDER BY` from the whitelisted expression and bind `:size` / `:offset` (or `:jobName` etc.) as parameters.

Do **not** use `JpaSort.unsafe(...)` or `Sort.by(...)` with a native query that has `GROUP BY` or aggregates: Hibernate prefixes the sort expression with the FROM alias, producing things like `ORDER BY je.je.job_execution_id` that fail at runtime. The custom-fragment route avoids that entirely.

## Models (response shapes)

`dashboard/model/*` are Java records — immutable, no Lombok needed. Naming follows the dashboard tile they're sized for: `RunCounts`, `JobRunPage`, `IoSummary`, `StepDetail`, etc. Frontend types in [`src/types/`](../frontend/src/types/) and per-page `types.ts` files mirror these field-for-field; keep them in sync when changing either side.

For paginated endpoints the response shape is `{ content, page, size, totalElements }` — see [JobRunPage](src/main/java/com/guavasoft/springbatch/dashboard/model/JobRunPage.java).

## Mappers

MapStruct (`@Mapper(componentModel = "spring")`) generates `*Impl` classes at compile time into `target/generated-sources/annotations/`. The annotation processor path in `pom.xml` lists Lombok first, then `lombok-mapstruct-binding`, then `mapstruct-processor` — order matters because Lombok must run first so MapStruct sees the generated getters.

VSCode-with-Eclipse-JDT gotcha: if the JDT workspace's annotation-processor cache goes stale, JDT compiles the generated `*Impl.java` without resolving its imports and writes a stub `.class` over Maven's good output. Symptom: `NoSuchBeanDefinitionException: No qualifying bean of type ...Mapper`. Fix: VSCode → Command Palette → "Java: Clean Java Language Server Workspace" → restart.

## Error handling

[GlobalExceptionHandler](src/main/java/com/guavasoft/springbatch/dashboard/config/GlobalExceptionHandler.java) is `@RestControllerAdvice`:

- `ConstraintViolationException` → 400 with generic `"Invalid request parameters."`. Logged at DEBUG.
- `Exception` (catch-all) → 500 with generic `"An unexpected error occurred."`. Full stack logged at ERROR.

The body shape is `{ timestamp, status, error, message, path }` — never includes class names or SQL. If you add new error categories, prefer dedicated `@ExceptionHandler`s with specific status codes over leaking through to the catch-all.

## Security

[SecurityConfig](src/main/java/com/guavasoft/springbatch/dashboard/config/SecurityConfig.java) — OAuth2 login via GitHub, success URL `${app.oauth2.success-url}`. CORS allows `${app.cors.allowed-origins}` with credentials. CSRF is disabled only for `/api/logout` (the frontend hits it as a plain POST).

`/api/auth/me`, `/api/logout`, `/`, `/error`, and the OAuth2 callback paths are `permitAll`. Everything else requires authentication.

GitHub credentials live in `.env` (`GITHUB_CLIENT_ID`, `GITHUB_CLIENT_SECRET`) — git-ignored.

## Running

```bash
./mvnw spring-boot:run
```

Postgres is brought up by `spring-boot-docker-compose` from [compose.yaml](compose.yaml) on first run. `db/init/` SQL is applied to the container's initdb step the first time the volume is created. After schema changes, `docker compose down -v` to drop the volume and re-init.

## Testing

```bash
./mvnw test
```

[TestcontainersConfiguration](src/test/java/com/guavasoft/springbatch/TestcontainersConfiguration.java) provisions an ephemeral Postgres via `@ServiceConnection`. Test classes import it via `@Import(TestcontainersConfiguration.class)` on a `@SpringBootTest`. The `BATCH_*` schema is auto-applied by Spring Batch on startup.

## Conventions

- Records, not Lombok-annotated classes, for response models.
- `@RequiredArgsConstructor` for services/repositories with `final` fields (avoids field injection).
- Don't introduce `org.springframework.beans.factory.annotation.Autowired` field injection.
- Native SQL only when JPA can't express it cleanly. Always parameterize values; build dynamic clauses (ORDER BY, columns) from a whitelist.
- Don't return JPA entities from controllers; map to a record first.
- Keep controllers free of business logic — anything past parameter parsing and a service call belongs in the service.
- New endpoints get an `@Operation(summary = "...", description = "...")` so SpringDoc shows them.
