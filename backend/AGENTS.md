# Backend agent guide

Spring Boot 4 service that exposes the Spring Batch metadata as a REST API for the dashboard frontend. Reads from one or more PostgreSQL **or** MySQL databases that already host `BATCH_*` metadata tables (the schema Spring Batch creates for itself). Doesn't write to those tables. Exactly one engine is active per build (Maven profile selects the bundled JDBC driver); mixing engines in one boot is not supported.

## Stack

- Java 21, Spring Boot 4.0.x, Spring Framework 7
- Spring MVC (`spring-boot-starter-webmvc`)
- Spring Data JPA + Hibernate 6 for entity-based reads (`BATCH_JOB_INSTANCE`, `BATCH_JOB_EXECUTION`, `BATCH_STEP_EXECUTION`, etc.)
- `NamedParameterJdbcTemplate` for any read where dynamic `ORDER BY` / aggregates are needed (Spring Data's `Sort` rewrites property paths in unsafe ways for native queries — see [Repository conventions](#repository-conventions))
- MapStruct for DTO mapping (Lombok + MapStruct annotation processors wired in `pom.xml`)
- Spring Security OAuth2 client (GitHub login by default, any provider configurable) — session-cookie auth, frontend gets a `JSESSIONID`
- SpringDoc OpenAPI — interactive UI at [`http://localhost:8080/swagger-ui/index.html`](http://localhost:8080/swagger-ui/index.html), raw spec at `/v3/api-docs`
- Apache Commons Lang3 for null-safe string helpers (`StringUtils.isBlank`, `EqualsBuilder`, etc.)
- Testcontainers + Spring Boot Testcontainers for integration tests
- Build: Maven wrapper (`./mvnw`)

## Layout

```
src/main/java/com/guavasoft/springbatch/
  dashboard/
    DashboardApplication.java   @SpringBootApplication entry point
    config/
      SecurityConfig.java                  SecurityFilterChain (OAuth2 login + permitAll on /api/auth/me + allow-list)
      AuthProperties.java                  binds app.auth.* (allowed-logins + provider attribute mapping)
      DatasourcesProperties.java           binds app.datasources from application-local-*.yml
      DynamicDataSourceConfig.java         AbstractRoutingDataSource over the configured pool
      DataSourceContext.java               ThreadLocal lookup-key holder
      DataSourceContextFilter.java         reads X-Environment request header → DataSourceContext
      GlobalExceptionHandler.java          @RestControllerAdvice; never leaks SQL/stack traces
    dialect/
      SqlDialect.java                      strategy interface (duration math, NULLS LAST, etc.)
      PostgresqlDialect.java               @ConditionalOnProperty(app.dialect = POSTGRESQL)
      MysqlDialect.java                    @ConditionalOnProperty(app.dialect = MYSQL)
    controller/                            REST endpoints — AuthController (/api/auth/me) + dashboard endpoints
    service/                               business logic
    repository/                            Spring Data JPA + custom JdbcTemplate fragments
    entity/                                JPA entities mapped onto BATCH_* tables (read-only)
    entity/projection/                     interface-based Spring Data projections
    mapper/                                MapStruct mappers (entity/projection → model record)
    model/                                 immutable response records (formerly `dto/`)
db/init-postgresql/                        Postgres docker compose initdb scripts
db/init-mysql/                             MySQL docker compose initdb scripts (upstream Spring Batch DDL, uppercase)
```

## Database engine selection

Choose the engine once, at build time, via a Maven profile. Each profile pulls in its driver, sets `app.dialect`, and activates the matching local Spring profile.

| | Postgres (default) | MySQL |
|---|---|---|
| Build | `./mvnw …` | `./mvnw -Pmysql …` |
| Run | `./mvnw spring-boot:run` | `./mvnw -Pmysql spring-boot:run` |
| Test | `./mvnw test` | `./mvnw -Pmysql test` |

The profile sets `APP_DIALECT` and `LOCAL_PROFILE` env vars on the forked JVM (for `spring-boot:run`) and `app.dialect` / `spring.profiles.active` system properties (for surefire), so a plain CLI invocation Just Works.

Three pieces fit together:

1. **`app.dialect`** (`POSTGRESQL` | `MYSQL`) selects the active [`SqlDialect`](src/main/java/com/guavasoft/springbatch/dashboard/dialect/SqlDialect.java) bean. The two impls are gated on `@ConditionalOnProperty`, so exactly one is registered per boot. Misconfiguration fails fast with `No qualifying bean of type 'SqlDialect'`.
2. **`spring.profiles.active`** (`local-postgresql` | `local-mysql`) loads the matching `application-local-*.yml`, which holds the `app.datasources` entries with the right JDBC URLs.
3. **Hibernate naming** — entities use uppercase `@Table(name = "BATCH_*")` and explicit `@Column(name = "snake_case")`. Hibernate uses `PhysicalNamingStrategyStandardImpl` (preserves names verbatim) so MySQL on case-sensitive filesystems finds the upstream-uppercase Spring Batch tables. Postgres folds unquoted identifiers itself, so it doesn't notice.

When writing native SQL, always go through `SqlDialect` for parts that diverge between engines:

- `dialect.durationSeconds(start, end)` — epoch-diff math (`EXTRACT(EPOCH …)::bigint` vs `TIMESTAMPDIFF(SECOND, …)`)
- `dialect.orderByNullsLast(expr, dir)` — Postgres has `NULLS LAST`, MySQL emulates with `(expr IS NULL), expr`
- `dialect.avgDurationSeconds`, `maxDurationSeconds`, `sumDurationSeconds` — same idea for aggregates

Anything else stays portable. `COUNT(*) FILTER (WHERE …)` is Postgres-only — rewrite as `SUM(CASE WHEN … THEN 1 ELSE 0 END)` (see existing custom impls).

## Multi-environment / dynamic datasource

Each frontend request includes an `X-Environment: <name>` header (set by the axios interceptor based on the user's selection). The backend wires this through:

1. [DataSourceContextFilter](src/main/java/com/guavasoft/springbatch/dashboard/config/DataSourceContextFilter.java) — `OncePerRequestFilter` that copies the header into a ThreadLocal `DataSourceContext`.
2. [DynamicDataSourceConfig](src/main/java/com/guavasoft/springbatch/dashboard/config/DynamicDataSourceConfig.java) — `AbstractRoutingDataSource` whose `determineCurrentLookupKey()` reads `DataSourceContext.get()`.
3. Available environments come from `app.datasources` in [application-local-postgresql.yml](src/main/resources/application-local-postgresql.yml) / [application-local-mysql.yml](src/main/resources/application-local-mysql.yml) (name, url, username, password). The first entry is the default when no header / unknown header is supplied.

Adding a new environment: add another item under `app.datasources` (matching the active engine — multiple entries of the *same* type are fine) and restart. The frontend's `EnvironmentSelector` picks it up automatically from `GET /api/environments`.

## Controller / service / repository pattern

- **Controller** ([dashboard/controller/](src/main/java/com/guavasoft/springbatch/dashboard/controller/)) — thin: `@RequestMapping`, `@PathVariable` / `@RequestParam` validation, `@Operation` for OpenAPI, calls service. Use `@Validated` on the class to enable Bean Validation on parameters; pair with `@Min`, `@Max`, `@NotBlank`, etc.
- **Service** ([dashboard/service/](src/main/java/com/guavasoft/springbatch/dashboard/service/)) — `@Service` + `@Transactional(readOnly = true)`. Coordinates repositories and mappers; never returns entities directly.
- **Repository** — see below.

## Repository conventions

Two repository styles in use; pick based on shape of the query:

### Spring Data JPA repository

Use for straightforward reads against an entity, including derived queries and named JPQL/native `@Query` with **fixed** ordering. Example: [JobInstanceRepository](src/main/java/com/guavasoft/springbatch/dashboard/repository/JobInstanceRepository.java). Keep JPQL portable across engines — no `NULLS LAST` (use `CASE WHEN … IS NULL THEN 1 ELSE 0 END`), no engine-specific functions.

### `JdbcTemplate`-backed custom fragment

Use when you need any of: dynamic ORDER BY, dynamic LIMIT/OFFSET pagination, aggregate ordering, dialect-specific SQL (epoch diff, NULLS LAST), or hand-rolled SQL projection that doesn't map cleanly to an entity / interface projection.

Pattern (see [JobExecutionRepositoryCustom](src/main/java/com/guavasoft/springbatch/dashboard/repository/JobExecutionRepositoryCustom.java) + [JobExecutionRepositoryCustomImpl](src/main/java/com/guavasoft/springbatch/dashboard/repository/JobExecutionRepositoryCustomImpl.java)):

1. Declare a `*Custom` interface with the dynamic methods.
2. Have the JPA repository extend both `JpaRepository<E, ID>` and the custom interface — Spring Data wires the impl automatically.
3. Implement the custom interface as a `@Repository` class taking `NamedParameterJdbcTemplate` and `SqlDialect` via constructor.
4. Whitelist allowed sort fields → SQL expression in a `Map<String, String>` *inside the impl*, never accept raw SQL from the controller.
5. Build the `ORDER BY` from the whitelisted expression and bind `:size` / `:offset` (or `:jobName` etc.) as parameters. For duration sorts, swap the whitelist value with `dialect.durationSeconds(...)` at build time.

Do **not** use `JpaSort.unsafe(...)` or `Sort.by(...)` with a native query that has `GROUP BY` or aggregates: Hibernate prefixes the sort expression with the FROM alias, producing things like `ORDER BY je.je.job_execution_id` that fail at runtime. The custom-fragment route avoids that entirely.

## Models (response shapes)

`dashboard/model/*` are Java records — immutable, no Lombok needed. Naming follows the dashboard tile they're sized for: `RunCounts`, `JobRunPage`, `IoSummary`, `StepDetail`, etc. Frontend types in [`src/types/`](../frontend/src/types/) and per-page `types.ts` files mirror these field-for-field; keep them in sync when changing either side.

For paginated endpoints the response shape is `{ content, page, size, totalElements }` — see [JobRunPage](src/main/java/com/guavasoft/springbatch/dashboard/model/JobRunPage.java).

The [`BatchStatus`](src/main/java/com/guavasoft/springbatch/dashboard/entity/BatchStatus.java) enum carries its own `label` and `color` per constant — adding a new status (e.g. `STOPPED`) means adding a single enum constant and the chart endpoints pick it up automatically. Don't hardcode chart labels/colors at the call site; iterate `BatchStatus.values()`.

## Mappers

MapStruct (`@Mapper(componentModel = "spring")`) generates `*Impl` classes at compile time into `target/generated-sources/annotations/`. The annotation processor path in `pom.xml` lists Lombok first, then `lombok-mapstruct-binding`, then `mapstruct-processor` — order matters because Lombok must run first so MapStruct sees the generated getters.

VSCode-with-Eclipse-JDT gotcha: if the JDT workspace's annotation-processor cache goes stale, JDT compiles the generated `*Impl.java` without resolving its imports and writes a stub `.class` over Maven's good output. Symptom: `NoSuchBeanDefinitionException: No qualifying bean of type ...Mapper`. The pom now sets `<m2e.apt.activation>jdt_apt</m2e.apt.activation>` to make m2e run MapStruct's APT through JDT, which avoids the stub. If you still hit it: VSCode → Command Palette → "Java: Clean Java Language Server Workspace" → "Restart and delete".

## Error handling

[GlobalExceptionHandler](src/main/java/com/guavasoft/springbatch/dashboard/config/GlobalExceptionHandler.java) is `@RestControllerAdvice`:

- `ConstraintViolationException` → 400 with generic `"Invalid request parameters."`. Logged at DEBUG.
- `Exception` (catch-all) → 500 with generic `"An unexpected error occurred."`. Full stack logged at ERROR.

The body shape is `{ timestamp, status, error, message, path }` — never includes class names or SQL. If you add new error categories, prefer dedicated `@ExceptionHandler`s with specific status codes over leaking through to the catch-all.

## Security

[SecurityConfig](src/main/java/com/guavasoft/springbatch/dashboard/config/SecurityConfig.java) — OAuth2 login (defaults to GitHub), success URL `${app.oauth2.success-url}`. CORS allows `${app.cors.allowed-origins}` with credentials. CSRF is disabled only for `/api/logout` (the frontend hits it as a plain POST).

`/api/auth/me`, `/api/logout`, `/`, `/error`, and the OAuth2 callback paths are `permitAll`. Everything else requires authentication.

[AuthProperties](src/main/java/com/guavasoft/springbatch/dashboard/config/AuthProperties.java) (`app.auth.*`) controls two things:

- `attributes.{login,name,avatar-url}` — provider attribute names that populate the fixed `/api/auth/me` response shape. Defaults match GitHub; override for other providers (e.g. Google: `login=email`, `avatar-url=picture`).
- `allowed-logins` — optional comma-delimited allow-list. Empty (default) admits any authenticated user. When non-empty, [SecurityConfig's custom `OAuth2UserService`](src/main/java/com/guavasoft/springbatch/dashboard/config/SecurityConfig.java) rejects logins outside the list with `access_denied` *before* a session is created.

OAuth client credentials live in `.env` (`GITHUB_CLIENT_ID`, `GITHUB_CLIENT_SECRET`) — git-ignored.

## Running

```bash
./mvnw spring-boot:run               # Postgres (default)
./mvnw -Pmysql spring-boot:run       # MySQL
```

Postgres and MySQL are both brought up by `spring-boot-docker-compose` from [compose.yaml](compose.yaml) on first run. `db/init-postgresql/` and `db/init-mysql/` SQL is applied to each container's initdb step the first time the volume is created. After schema changes, `docker compose down -v` to drop the volumes and re-init (or `docker volume rm backend_<name>-data` to drop just one).

## Testing

```bash
./mvnw test                          # Postgres
./mvnw -Pmysql test                  # MySQL
./mvnw verify                        # full build incl. coverage report
```

[TestcontainersConfiguration](src/test/java/com/guavasoft/springbatch/dashboard/TestcontainersConfiguration.java) declares both a `PostgreSQLContainer` and a `MySQLContainer`, each gated on `@ConditionalOnProperty(app.dialect)`. The active Maven profile sets `app.dialect` for surefire so only the matching container spins up. Each container mounts its own `db/init-*/` scripts into `/docker-entrypoint-initdb.d`, and a `DynamicPropertyRegistrar` binds the testcontainer's host/port/credentials onto the env-var placeholders that `application-local-*.yml` consumes.

Test layers in this repo:

- **Unit tests** (services, mappers, dialects, the `ThroughputMetric` enum) — plain JUnit 5; services use `@ExtendWith(MockitoExtension.class)` + `@Mock` + `@InjectMocks` to fake their repository / mapper deps.
- **WebMvc slice tests** (controllers) — `@WebMvcTest(controllers = X.class)` + `@AutoConfigureMockMvc(addFilters = false)` to bypass security; service deps mocked with `@MockitoBean` (Spring Framework 6.2+ replacement for `@MockBean`). Imports come from `org.springframework.boot.webmvc.test.autoconfigure` in Boot 4.
- **JPA slice tests** (repositories) — share the [`@BatchRepositoryTest`](src/test/java/com/guavasoft/springbatch/dashboard/repository/BatchRepositoryTest.java) meta-annotation: `@DataJpaTest` + `@AutoConfigureTestDatabase(replace = NONE)` + imports for the dynamic datasource, the active dialect, and the custom JDBC repo impls. Tests run against the Testcontainers DB seeded by `db/init-*/02-seed.sql`.

### Coverage

JaCoCo is bound to `verify`. Coverage data is emitted as `target/jacoco.exec` and rendered to `target/site/jacoco/`.

CI runs the matrix (Postgres + MySQL), uploads each profile's `jacoco.exec` as an artifact, then a downstream `coverage` job merges them via `jacoco:merge@jacoco-merge` + `jacoco:report@jacoco-report-merged` into `target/site/jacoco-merged/jacoco.xml`. The merged XML is consumed by [`PavanMudigonda/jacoco-reporter`](../.github/workflows/pull-request.yml), which enforces an **80% overall + 80% changed-files** threshold and posts a per-package per-counter table on the PR. Threshold checking lives entirely in the action — there's no pom-side `jacoco:check` execution to bypass when one matrix entry exercises paths the other doesn't.

Excluded from coverage in [pom.xml](pom.xml): `DashboardApplication`, the `config/`, `entity/`, `model/` packages, and MapStruct-generated `*MapperImpl` classes.

CI runs both engines as a matrix in [`.github/workflows/pull-request.yml`](../.github/workflows/pull-request.yml) (`fail-fast: false`).

## Conventions

- Records, not Lombok-annotated classes, for response models.
- `@RequiredArgsConstructor` for services/repositories with `final` fields (avoids field injection).
- Don't introduce `org.springframework.beans.factory.annotation.Autowired` field injection.
- Native SQL only when JPA can't express it cleanly. Always parameterize values; build dynamic clauses (ORDER BY, columns) from a whitelist; route engine-specific fragments through `SqlDialect`.
- Don't return JPA entities from controllers; map to a record first.
- Keep controllers free of business logic — anything past parameter parsing and a service call belongs in the service.
- New endpoints get an `@Operation(summary = "...", description = "...")` so SpringDoc shows them.
- Variable naming: prefer expressive names over abbreviations (`throughputBars` over `bars`, `stepDetails` over `details`). Single-letter / short names are fine for lambda parameters and generic type parameters where the surrounding context makes the meaning obvious.
