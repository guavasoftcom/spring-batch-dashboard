# Backend agent guide

Spring Boot 4 service that exposes the Spring Batch metadata as a REST API for the dashboard frontend. Reads from one or more PostgreSQL, MySQL, Oracle, **and / or** SQL Server databases that already host `BATCH_*` metadata tables (the schema Spring Batch creates for itself). Doesn't write to those tables. All four JDBC drivers ship in the same artifact, so a single deployment can serve mixed engines simultaneously — each `app.datasources` entry declares its own `type` and the matching dialect is selected per request.

## Stack

- Java 21, Spring Boot 4.0.x, Spring Framework 7
- Spring MVC (`spring-boot-starter-webmvc`)
- Spring Data JPA + Hibernate 6 for entity-based reads (`BATCH_JOB_INSTANCE`, `BATCH_JOB_EXECUTION`, `BATCH_STEP_EXECUTION`, etc.)
- `NamedParameterJdbcTemplate` for any read where dynamic `ORDER BY` / aggregates are needed (Spring Data's `Sort` rewrites property paths in unsafe ways for native queries — see [Repository conventions](#repository-conventions))
- MapStruct for DTO mapping (Lombok + MapStruct annotation processors wired in `pom.xml`)
- Spring Security OAuth2 client (GitHub login by default, any provider configurable) — session-cookie auth, frontend gets a `JSESSIONID`
- Spring Boot Actuator — only the `health` endpoint is exposed; Kubernetes probe groups (`/actuator/health/{readiness,liveness}`) are enabled and the auto DataSource indicator is disabled (the primary bean is a routing DataSource that needs a per-request ThreadLocal). See [`application.yml`](src/main/resources/application.yml).
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
      SecurityConfig.java                  SecurityFilterChain (OAuth2 login, /api/** authenticated, SPA shell + static permitAll, optional allow-list)
      AuthProperties.java                  binds app.auth.* (allowed-logins + provider attribute mapping)
      OAuth2Properties.java                binds app.oauth2.* (success-url + per-registration button label/color/icon)
      DatasourcesProperties.java           binds app.datasources (each entry: name, type, url, username, password, schema)
      DynamicDataSourceConfig.java         AbstractRoutingDataSource + RoutingSqlDialect builders
      DataSourceContext.java               ThreadLocal lookup-key holder
      DataSourceContextFilter.java         reads X-Environment request header → DataSourceContext
      GlobalExceptionHandler.java          @RestControllerAdvice; never leaks SQL/stack traces
      TimestampFormatter.java              formats LocalDateTime → ISO-8601 UTC instant using
                                           the active datasource's configured timezone
    dialect/
      DialectType.java                     POSTGRESQL | MYSQL | ORACLE | SQLSERVER — declared per-datasource
      SqlDialect.java                      strategy interface (duration math, NULLS LAST, etc.)
      PostgresqlDialect.java               @Component, stateless
      MysqlDialect.java                    @Component, stateless
      OracleDialect.java                   @Component, stateless
      SqlServerDialect.java                @Component, stateless
      RoutingSqlDialect.java               @Primary facade — delegates per DataSourceContext
    controller/                            REST endpoints — AuthController (/api/auth/me, /api/auth/providers) + dashboard endpoints; SpaController forwards SPA routes to /index.html
    service/                               business logic
    repository/                            Spring Data JPA + custom JdbcTemplate fragments
    entity/                                JPA entities mapped onto BATCH_* tables (read-only)
    entity/projection/                     interface-based Spring Data projections
    mapper/                                MapStruct mappers (entity/projection → model record)
    model/                                 immutable response records (formerly `dto/`)
db/init-postgresql/                        Postgres docker compose initdb scripts
db/init-mysql/                             MySQL docker compose initdb scripts (upstream Spring Batch DDL, uppercase)
db/init-oracle/                            Oracle (gvenzl/oracle-free) initdb scripts (upstream Spring Batch DDL)
db/init-sqlserver/                         SQL Server initdb scripts (upstream Spring Batch DDL; GO separators preserved)
```

## Multi-engine datasources

Each `app.datasources` entry declares its engine via `type: POSTGRESQL | MYSQL | ORACLE | SQLSERVER`, and all four JDBC drivers are bundled at runtime. A single deployment can mix engines — entries can be all of the same type, or any combination. The frontend's `EnvironmentSelector` switches between them via the `X-Environment` request header.

The wiring:

1. [DataSourceContextFilter](src/main/java/com/guavasoft/springbatch/dashboard/config/DataSourceContextFilter.java) — `OncePerRequestFilter` that copies `X-Environment` into a ThreadLocal `DataSourceContext`.
2. [DynamicDataSourceConfig](src/main/java/com/guavasoft/springbatch/dashboard/config/DynamicDataSourceConfig.java) — exposes:
   - an `AbstractRoutingDataSource` (`@Primary DataSource`) whose `determineCurrentLookupKey()` reads `DataSourceContext.get()`
   - a `Map<String, SqlDialect>` keyed by datasource name, derived from each entry's `type`
   - a [RoutingSqlDialect](src/main/java/com/guavasoft/springbatch/dashboard/dialect/RoutingSqlDialect.java) (`@Primary SqlDialect`) that delegates each call to the dialect for the currently-bound datasource
3. Repositories inject `SqlDialect` and call `dialect.durationSeconds(...)` etc. — the routing facade picks the right per-engine SQL based on the active `DataSourceContext`.
4. The first `app.datasources` entry is the default (used when no header is supplied or the header value is unknown).

When writing native SQL, always go through `SqlDialect` for parts that diverge between engines:

- `dialect.durationSeconds(start, end)` — epoch-diff math (`EXTRACT(EPOCH …)::bigint` on Postgres, `TIMESTAMPDIFF(SECOND, …)` on MySQL, `(CAST(end AS DATE) - CAST(start AS DATE)) * 86400` on Oracle, `DATEDIFF(SECOND, …)` on SQL Server)
- `dialect.orderByNullsLast(expr, dir)` — Postgres and Oracle have `NULLS LAST`; MySQL emulates with `(expr IS NULL), expr`; SQL Server emulates with `CASE WHEN expr IS NULL THEN 1 ELSE 0 END, expr` (it doesn't accept `(expr IS NULL)` as a value expression like MySQL does)
- `dialect.avgDurationSeconds`, `maxDurationSeconds`, `sumDurationSeconds` — same idea for aggregates
- `dialect.truncateToDay(expr)` — calendar-day bucketing for `GROUP BY` (`DATE_TRUNC('day', expr)::date` on Postgres, `DATE(expr)` on MySQL, `TRUNC(expr)` on Oracle, `CAST(expr AS DATE)` on SQL Server); bucketing happens in the database's local zone — convert the column first if you need zone-aware bucketing
- `dialect.paginationClause(size, offset)` — `LIMIT … OFFSET …` on Postgres / MySQL; ANSI `OFFSET … FETCH NEXT …` on Oracle and SQL Server
- `dialect.setSchemaSql(schema)` — connection-init SQL for the per-datasource schema (see below)

Anything else stays portable. `COUNT(*) FILTER (WHERE …)` is Postgres-only — rewrite as `SUM(CASE WHEN … THEN 1 ELSE 0 END)` (see existing custom impls). One-arg `ROUND(x)` is invalid on SQL Server (it requires `ROUND(x, length)`) — use `FLOOR(x + 0.5)` for integer rounding, which is portable across all four engines.

### Hibernate caveat

Hibernate detects its dialect once, on the first JDBC connection it acquires from the routing data source (whichever `app.datasources` entry is first). All Spring Data JPA queries — derived methods, `@Query` JPQL, native `@Query` — are generated against that cached dialect. That's fine for portable SQL (basic SELECT / COUNT / aggregations across PG / MySQL / Oracle / SQL Server), but engine-specific JPA features such as **`Pageable`-driven LIMIT / OFFSET** assume the cached dialect's pagination syntax. PG and MySQL share `LIMIT … OFFSET …`; Oracle and SQL Server use `OFFSET … FETCH NEXT …`. Cross-engine routing for those queries is *not* supported — when you need engine-aware SQL, write it through the JdbcTemplate fragment + `SqlDialect` path instead.

### Per-datasource schema

Set `app.datasources[*].schema` when the `BATCH_*` tables don't live in the engine's default schema. The repository SQL is unqualified by design; the schema is applied as connection-init SQL on each pooled connection, dialect-specific:

- **Postgres** → `SET search_path TO <schema>`
- **Oracle** → `ALTER SESSION SET CURRENT_SCHEMA = <schema>`
- **MySQL** → no init SQL (the database name in the JDBC URL plays the same role; leave `schema` unset)
- **SQL Server** → no init SQL. SQL Server has no per-session default-schema knob; the database in the JDBC URL plays the schema role and the `BATCH_*` tables must live in the user's default schema (typically `dbo`). Leave `schema` unset.

[DynamicDataSourceConfig](src/main/java/com/guavasoft/springbatch/dashboard/config/DynamicDataSourceConfig.java) validates the value against `^[A-Za-z_][A-Za-z0-9_$]*$` (max 128 chars) at bean creation, so the identifier can be safely concatenated into the init SQL — anything else fails the boot loudly. Hibernate's `@Table` lookups also pick up the connection-default schema, so JPA queries respect the same setting without per-entity changes.

### Per-datasource timezone

Set `app.datasources[*].timezone` to the IANA zone (e.g. `America/New_York`) the database stores its zone-less `TIMESTAMP` columns in. Blank or unset defaults to `UTC`. The value is parsed once at boot via `ZoneId.of(...)`; bad zones fail startup with a clear `IllegalStateException`. Resolved zones are exposed as a `Map<String, ZoneId> datasourceZoneIds` bean keyed by datasource name.

[TimestampFormatter](src/main/java/com/guavasoft/springbatch/dashboard/config/TimestampFormatter.java) is the single edge-conversion seam — inject it wherever a `LocalDateTime` from the DB needs to leave the API. It interprets the value in the active datasource's zone (read from `DataSourceContext`) and emits an ISO-8601 UTC instant via `DateTimeFormatter.ISO_INSTANT`. All four pre-existing emission sites (`StepDetailRowMapper`, `StepExecutionRepositoryCustomImpl`, `JobExecutionStepsService`, `JobRunMapper`) route through it; new endpoints emitting timestamps should do the same instead of formatting locally. `JobRunMapper` wires the formatter via `@Mapper(uses = TimestampFormatter.class)` so MapStruct picks the helper for any `LocalDateTime → String` mapping.

## Controller / service / repository pattern

- **Controller** ([dashboard/controller/](src/main/java/com/guavasoft/springbatch/dashboard/controller/)) — thin: `@RequestMapping`, `@PathVariable` / `@RequestParam` validation, `@Operation` for OpenAPI, calls service. Use `@Validated` on the class to enable Bean Validation on parameters; pair with `@Min`, `@Max`, `@NotBlank`, etc.
- **Service** ([dashboard/service/](src/main/java/com/guavasoft/springbatch/dashboard/service/)) — `@Service` + `@Transactional(readOnly = true)`. Coordinates repositories and mappers; never returns entities directly.
- **Repository** — see below.

## Repository conventions

Two repository styles in use; pick based on shape of the query:

### Spring Data JPA repository

Use for straightforward reads against an entity, including derived queries and named JPQL/native `@Query` with **fixed** ordering. Example: [JobInstanceRepository](src/main/java/com/guavasoft/springbatch/dashboard/repository/JobInstanceRepository.java). Keep JPQL portable across engines — no `NULLS LAST` (use `CASE WHEN … IS NULL THEN 1 ELSE 0 END`), no engine-specific functions. Do not use `Pageable` if the query needs to work across engines — see the [Hibernate caveat](#hibernate-caveat).

### `JdbcTemplate`-backed custom fragment

Use when you need any of: dynamic ORDER BY, dynamic LIMIT/OFFSET pagination, aggregate ordering, dialect-specific SQL (epoch diff, NULLS LAST), or hand-rolled SQL projection that doesn't map cleanly to an entity / interface projection. **All cross-engine queries belong here** — the `SqlDialect` facade is what makes them work against mixed datasources.

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

Timestamp record fields are `String`s carrying ISO-8601 UTC instants (`2026-04-30T14:30:00Z`), produced by `TimestampFormatter` from the DB's `LocalDateTime` interpreted in the active datasource's `timezone`. Schema descriptions/examples should reflect that — don't ship local-time strings.

Each record carries SpringDoc/OpenAPI annotations (`io.swagger.v3.oas.annotations.media.Schema`) at both the class level (description) and per-component level (description, `example`, `nullable`, `allowableValues` where appropriate). These power the Swagger UI body schemas at `/swagger-ui/index.html`; when adding a new response record, follow the existing pattern — class `@Schema(description = "…")` plus a per-parameter `@Schema(description = "…", example = "…")` on every component.

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

`/api/**` requires authentication, with `/api/auth/me`, `/api/auth/providers`, and `/api/logout` open. Everything else — the SPA shell, static assets bundled at `classpath:/static/`, OAuth2 callback paths, and `/error` — is `permitAll`, since the SPA enforces auth at runtime via `/api/auth/me`. SPA deep links (`/overview`, `/jobs/...`) are forwarded to `/index.html` by [`SpaController`](src/main/java/com/guavasoft/springbatch/dashboard/controller/SpaController.java) so React Router resolves them on hard refresh.

[`/api/auth/providers`](src/main/java/com/guavasoft/springbatch/dashboard/controller/AuthController.java) iterates the `ClientRegistrationRepository` and returns one [`OAuth2Provider`](src/main/java/com/guavasoft/springbatch/dashboard/model/OAuth2Provider.java) per registered provider so the frontend's login page can render a button per provider. Per-button display is sourced from [OAuth2Properties](src/main/java/com/guavasoft/springbatch/dashboard/config/OAuth2Properties.java) (`app.oauth2.buttons.<registrationId>.{label,color,iconUrl}`); missing entries fall back to a capitalized registration id with no color/icon.

Unauthenticated `/api/**` requests get a clean **401** via a scoped `HttpStatusEntryPoint` (instead of being redirected through the OAuth2 entry-point chain, which produced a 404). This matches the contract the frontend's axios interceptor expects — see [`frontend/src/config/client.ts`](../frontend/src/config/client.ts), which redirects to `/` on a 401.

[AuthProperties](src/main/java/com/guavasoft/springbatch/dashboard/config/AuthProperties.java) (`app.auth.*`) controls two things:

- `attributes.{login,name,avatar-url}` — provider attribute names that populate the fixed `/api/auth/me` response shape. Defaults match GitHub; override for other providers (e.g. Google: `login=email`, `avatar-url=picture`).
- `allowed-logins` — optional comma-delimited allow-list. Empty (default) admits any authenticated user. When non-empty, [SecurityConfig's custom `OAuth2UserService`](src/main/java/com/guavasoft/springbatch/dashboard/config/SecurityConfig.java) rejects logins outside the list with `access_denied` *before* a session is created.

OAuth client credentials live in `.env` (`GITHUB_CLIENT_ID`, `GITHUB_CLIENT_SECRET`) — git-ignored.

## Running

```bash
./mvnw spring-boot:run               # uses the `local` profile (all four engines)
```

The `local` profile ([application-local.yml](src/main/resources/application-local.yml)) declares one `app.datasources` entry per supported engine, each pointing at the matching docker-compose container, so the running app sees Postgres / MySQL / Oracle / SQL Server simultaneously and the frontend's `EnvironmentSelector` can flip between them. Production deployments override `app.datasources` directly and can list any combination of POSTGRESQL / MYSQL / ORACLE / SQLSERVER entries.

Postgres, MySQL, Oracle, and SQL Server are all brought up by `spring-boot-docker-compose` from [compose.yaml](compose.yaml) on first run. `db/init-postgresql/`, `db/init-mysql/`, and `db/init-oracle/` SQL is applied through each engine's native initdb hook; the SQL Server container has no equivalent, so its service runs a small `command:` wrapper that boots `sqlservr`, polls until ready, then applies `db/init-sqlserver/` via `sqlcmd`. After schema changes, `docker compose down -v` to drop the volumes and re-init (or `docker volume rm backend_<name>-data` to drop just one).

The Oracle dev container uses [gvenzl/oracle-free](https://github.com/gvenzl/oci-oracle-free) and the app connects as `SYSTEM` into the default pluggable database `FREEPDB1`. Init scripts under `db/init-oracle/` are dropped into `/container-entrypoint-initdb.d/` and run as `SYSTEM`, so the seeded `BATCH_*` tables live in the SYSTEM schema (fine for local dev — production deployments should point `app.datasources` at whatever schema actually owns the Spring Batch tables).

## Testing

```bash
./mvnw test                          # full unit + slice + repository suite
./mvnw verify                        # full build incl. coverage report
```

Surefire activates the `test` Spring profile (`spring.profiles.active=test`), which loads [src/test/resources/application-test.yml](src/test/resources/application-test.yml). That file declares one `app.datasources` entry per engine; the placeholders (`POSTGRES_HOST`, `MYSQL_HOST`, `ORACLE_HOST`, `SQLSERVER_HOST`, …) are bound at context refresh by the [`DynamicPropertyRegistrar`s](src/test/java/com/guavasoft/springbatch/dashboard/TestcontainersConfiguration.java) that boot the matching Testcontainers. The SQL Server container has no native init-script hook, so a separate `sqlServerSchemaInitializer` bean creates `mydatabase` and applies `db/init-sqlserver/{01-schema,02-seed}.sql` via Spring's `ScriptUtils` after the container starts; the registrar `@DependsOn`s it so test code only sees the datasource once it's seeded.

Test layers in this repo:

- **Unit tests** (services, mappers, dialects) — plain JUnit 5; services use `@ExtendWith(MockitoExtension.class)` + `@Mock` + `@InjectMocks` to fake their repository / mapper deps.
- **WebMvc slice tests** (controllers) — `@WebMvcTest(controllers = X.class)` + `@AutoConfigureMockMvc(addFilters = false)` to bypass security; service deps mocked with `@MockitoBean` (Spring Framework 6.2+ replacement for `@MockBean`). Imports come from `org.springframework.boot.webmvc.test.autoconfigure` in Boot 4.
- **JPA slice tests** (repositories) — share the [`@BatchRepositoryTest`](src/test/java/com/guavasoft/springbatch/dashboard/repository/BatchRepositoryTest.java) meta-annotation: `@DataJpaTest` + `@AutoConfigureTestDatabase(replace = NONE)` + imports for the dynamic datasource, every dialect impl plus the `RoutingSqlDialect` facade, and the custom JDBC repo impls. All four Testcontainers boot once per JUnit run; tests against `SqlDialect`-backed methods use the [`@AcrossDatasources`](src/test/java/com/guavasoft/springbatch/dashboard/repository/TestDatasources.java) meta-annotation, which fans out to PG / MySQL / Oracle / SQL Server by setting `DataSourceContext` on each parameter (a class-level `@AfterEach` clears the ThreadLocal). Tests of portable JPA-derived / JPQL queries stay as plain `@Test` and run once against the default datasource.

### Coverage

JaCoCo is bound to `verify`. Coverage data is emitted as `target/jacoco.exec` and rendered to `target/site/jacoco/`. CI feeds `target/site/jacoco/jacoco.xml` directly to [`PavanMudigonda/jacoco-reporter`](../.github/workflows/pull-request.yml), which enforces an **80% overall + 80% changed-files** threshold and posts a per-package per-counter table on the PR.

Excluded from coverage in [pom.xml](pom.xml): `DashboardApplication`, the `config/`, `entity/`, `model/` packages, and MapStruct-generated `*MapperImpl` classes.

## Conventions

- Records, not Lombok-annotated classes, for response models.
- `@RequiredArgsConstructor` for services/repositories with `final` fields (avoids field injection).
- Don't introduce `org.springframework.beans.factory.annotation.Autowired` field injection.
- Native SQL only when JPA can't express it cleanly. Always parameterize values; build dynamic clauses (ORDER BY, columns) from a whitelist; route engine-specific fragments through `SqlDialect`.
- Don't return JPA entities from controllers; map to a record first.
- Keep controllers free of business logic — anything past parameter parsing and a service call belongs in the service.
- New endpoints get an `@Operation(summary = "...", description = "...")` so SpringDoc shows them.
- Variable naming: prefer expressive names over abbreviations (`throughputBars` over `bars`, `stepDetails` over `details`). Single-letter / short names are fine for lambda parameters and generic type parameters where the surrounding context makes the meaning obvious.
