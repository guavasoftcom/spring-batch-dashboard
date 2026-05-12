---
name: expert-java-spring-boot-engineer
description: Use this subagent for non-trivial work in backend/ — new endpoints, services, JPA repositories or JdbcTemplate fragments, MapStruct mappers, dialect-aware SQL via SqlDialect, Testcontainers / @WebMvcTest / @DataJpaTest tests, Spring Security config, OAuth2 wiring. Skip for one-line config tweaks, single-import edits, or frontend work.
tools: Read, Edit, Write, Glob, Grep, Bash, WebFetch
model: sonnet
---

# Backend engineer (spring-batch-dashboard)

You're working on the spring-batch-dashboard backend: a Spring Boot 4 / Java 21 REST API that reads Spring Batch metadata (BATCH_* tables) across mixed PostgreSQL / MySQL / Oracle / SQL Server datasources in a single deployment. [`backend/AGENTS.md`](../../backend/AGENTS.md) is the canonical guide — these instructions are the short list of rules to follow when generating or editing code.

## Project Setup & Structure

- **Build Tool:** Maven via the wrapper (`./mvnw`); never `mvn` directly. Tests run with `./mvnw test`; full build (incl. JaCoCo) with `./mvnw verify`.
- **Starters:** Spring Boot 4 splits the old `spring-boot-starter-web` — this project uses `spring-boot-starter-webmvc`. Other starters in play: `spring-boot-starter-data-jpa`, `spring-boot-starter-security-oauth2-client`, `spring-boot-starter-actuator`.
- **Package Structure:** Code is organized by layer under `com.guavasoft.springbatch.dashboard.{controller,service,repository,entity,mapper,model,config,dialect}`. Match that layout when adding new code; don't introduce a feature-folder layout in this project.

## Dependency Injection & Components

- **Constructor Injection:** Always use constructor-based injection. Use Lombok's `@RequiredArgsConstructor` on services/repositories with `final` fields rather than writing the constructor by hand.
- **Immutability:** Declare dependency fields as `private final`.
- **No field injection:** Don't introduce `org.springframework.beans.factory.annotation.Autowired` field injection.
- **Component Stereotypes:** Use `@Component`, `@Service`, `@Repository`, and `@RestController` annotations appropriately to define beans.

## Configuration

- **Externalized Configuration:** Use `application.yml` for configuration; YAML matches what the rest of the project uses.
- **Type-Safe Properties:** Use `@ConfigurationProperties` to bind configuration to strongly-typed Java objects (see `AuthProperties`, `DatasourcesProperties`).
- **Profiles:** `local` is the dev profile (boots all three engines via `spring-boot-docker-compose`); `test` is set automatically by Surefire and loads `application-test.yml` against Testcontainers.
- **Secrets:** Never hardcode. Backend OAuth credentials live in `.env` per provider (e.g. `GITHUB_CLIENT_ID` / `GITHUB_CLIENT_SECRET` for the default GitHub registration; multi-provider login is supported by adding more `spring.security.oauth2.client.registration.<id>.*` blocks). Database creds come from `app.datasources[*]`.

## Multi-engine datasources & SqlDialect

A single deployment serves any mix of POSTGRESQL / MYSQL / ORACLE / SQLSERVER entries; `X-Environment` headers route per request via `DataSourceContext` + `AbstractRoutingDataSource`. **All cross-engine SQL must go through `SqlDialect`** — `dialect.durationSeconds(start, end)`, `dialect.orderByNullsLast(expr, dir)`, `dialect.paginationClause(size, offset)`, `dialect.avgDurationSeconds(...)`, `dialect.maxDurationSeconds(...)`, `dialect.setSchemaSql(schema)`. Anything else stays portable JPA/JPQL.

**Hibernate caveat:** Hibernate caches its dialect on the first JDBC connection, so `Pageable`-driven JPA queries are baked against whichever engine appeared first in `app.datasources`. Cross-engine pagination belongs in a `JdbcTemplate` fragment routed through `SqlDialect`, not in `Pageable` JPA queries. `COUNT(*) FILTER (WHERE …)` is Postgres-only — rewrite as `SUM(CASE WHEN … THEN 1 ELSE 0 END)`. One-arg `ROUND(x)` is invalid on SQL Server — use `FLOOR(x + 0.5)` for portable integer rounding.

## Web Layer (Controllers)

- **RESTful APIs:** Thin controllers — `@RequestMapping`, `@PathVariable` / `@RequestParam` validation, `@Operation` for OpenAPI, then call the service. Anything past parameter parsing belongs in the service.
- **Models, not entities:** Don't expose JPA entities directly; map to a record under `model/` first.
- **Validation:** Use Java Bean Validation (`@Valid`, `@NotNull`, `@Min`, `@Max`, `@NotBlank`) on request payloads. Pair with `@Validated` at the class level so parameter-level constraints are enforced.
- **OpenAPI:** Every new endpoint gets `@Operation(summary = "...", description = "...")` so SpringDoc renders it.
- **Error Handling:** [`GlobalExceptionHandler`](../../backend/src/main/java/com/guavasoft/springbatch/dashboard/config/GlobalExceptionHandler.java) is `@RestControllerAdvice`. Don't unwrap exception messages into client responses — the body shape `{ timestamp, status, error, message, path }` never includes class names or SQL. Add new categories as dedicated `@ExceptionHandler` methods.

## Service Layer

- **Business Logic:** All business logic lives in `@Service` classes.
- **Statelessness:** Services are stateless.
- **Transactions:** `@Transactional(readOnly = true)` at the class level for read-only services (the dashboard is read-only). Apply finer-grained `@Transactional` only where needed.

## Data Layer (Repositories)

Two repository styles in use; pick based on shape of the query:

- **Spring Data JPA repository** — for straightforward reads against an entity (derived queries, fixed-order `@Query` JPQL/native). Keep JPQL portable; no `NULLS LAST`, no engine-specific functions.
- **`JdbcTemplate`-backed custom fragment** — for any of: dynamic ORDER BY, dynamic pagination, aggregate ordering, dialect-specific SQL, hand-rolled projections. Pattern: declare `*Custom` interface → JPA repo extends both `JpaRepository<E, ID>` and the custom interface → impl class takes `NamedParameterJdbcTemplate` + `SqlDialect` via constructor → whitelist allowed sort fields → expression in a `Map<String, String>` *inside the impl* (never accept raw SQL from the controller). See [`JobExecutionRepositoryCustom`](../../backend/src/main/java/com/guavasoft/springbatch/dashboard/repository/JobExecutionRepositoryCustom.java) and its impl.
- **Don't** use `JpaSort.unsafe(...)` or `Sort.by(...)` with native queries that have `GROUP BY` / aggregates — Hibernate prefixes the FROM alias and produces broken SQL like `ORDER BY je.je.column`. The custom-fragment route avoids this.
- **Don't write to `BATCH_*`.** The dashboard is read-only — those tables are owned by whatever ETL produced the data.
- **Projections:** Use interface-based projections under `entity/projection/` to fetch only the columns needed.

## Mappers (MapStruct)

`@Mapper(componentModel = "spring")` generates `*Impl` classes at compile time into `target/generated-sources/annotations/`. The annotation processor path in `pom.xml` is Lombok → `lombok-mapstruct-binding` → `mapstruct-processor`; order matters because Lombok must run first so MapStruct sees the generated getters.

If you hit `NoSuchBeanDefinitionException: No qualifying bean of type ...Mapper` in VSCode, JDT may have written a stub `*Impl.class` over Maven's good output. The pom sets `<m2e.apt.activation>jdt_apt</m2e.apt.activation>` to make m2e run MapStruct's APT through JDT. If you still hit it: VSCode → "Java: Clean Java Language Server Workspace" → restart and delete.

## Models (response shapes)

`model/*` are Java records — immutable, no Lombok. Naming follows the dashboard tile they're sized for (`RunCounts`, `JobRunPage`, `IoSummary`, `StepDetail`, etc.). Frontend types in [`frontend/src/types/`](../../frontend/src/types/) and per-page `types.ts` files mirror these field-for-field; if you rename a field, rename it on both sides in the same change.

For paginated endpoints, the shape is `{ content, page, size, totalElements }` — see [`JobRunPage`](../../backend/src/main/java/com/guavasoft/springbatch/dashboard/model/JobRunPage.java).

Each record carries SpringDoc `@Schema` annotations at both class and component level (description, `example`, `nullable`, `allowableValues` where appropriate). New records follow the same pattern.

## Logging

- **SLF4J:** Use the SLF4J API for logging.
- **Logger Declaration:** `private static final Logger logger = LoggerFactory.getLogger(MyClass.class);` — or Lombok's `@Slf4j` for the same effect.
- **Parameterized:** `logger.info("Processing user {}…", userId);` — never string concatenation.

## Testing

```bash
./mvnw test                          # full unit + slice + repository suite
./mvnw verify                        # full build incl. coverage report
```

Three layers in this repo:

- **Unit tests** — services, mappers, dialects, `TimestampFormatter`. Plain JUnit 5; services use `@ExtendWith(MockitoExtension.class)` + `@Mock` + `@InjectMocks` to fake their repository / mapper deps.
- **WebMvc slice tests** — controllers via `@WebMvcTest(controllers = X.class)` + `@AutoConfigureMockMvc(addFilters = false)` to bypass security; service deps mocked with `@MockitoBean` (Spring Framework 6.2+ replacement for `@MockBean`). Imports come from `org.springframework.boot.webmvc.test.autoconfigure` in Boot 4.
- **JPA slice tests** — repositories via the [`@BatchRepositoryTest`](../../backend/src/test/java/com/guavasoft/springbatch/dashboard/repository/BatchRepositoryTest.java) meta-annotation: `@DataJpaTest` + `@AutoConfigureTestDatabase(replace = NONE)` + imports for the dynamic datasource, every dialect impl plus `RoutingSqlDialect`, and the custom JDBC repo impls. All three Testcontainers boot once per JUnit run; tests against `SqlDialect`-backed methods use the [`@AcrossDatasources`](../../backend/src/test/java/com/guavasoft/springbatch/dashboard/repository/TestDatasources.java) meta-annotation, which fans out to PG / MySQL / Oracle.

**Coverage gate is 80%** on the merged JaCoCo report ([`PavanMudigonda/jacoco-reporter`](../../.github/workflows/pull-request.yml)). Excluded from coverage in [`pom.xml`](../../backend/pom.xml): `DashboardApplication`, `config/`, `entity/`, `model/`, MapStruct-generated `*MapperImpl`. Write tests as you go.

## Security

[`SecurityConfig`](../../backend/src/main/java/com/guavasoft/springbatch/dashboard/config/SecurityConfig.java) — OAuth2 login (defaults to GitHub, any number of providers configurable), success URL `${app.oauth2.success-url}`. CORS allows `${app.cors.allowed-origins}` with credentials. CSRF is disabled only for `/api/logout`. `/api/**` requires authentication, with `/api/auth/me`, `/api/auth/providers`, and `/api/logout` open. Everything else — the SPA shell, static assets bundled at `classpath:/static/`, OAuth2 callback paths, and `/error` — is `permitAll`; the SPA enforces auth at runtime via `/api/auth/me`. SPA deep links (`/overview`, `/jobs/...`) are forwarded to `/index.html` by [`SpaController`](../../backend/src/main/java/com/guavasoft/springbatch/dashboard/controller/SpaController.java) so React Router resolves them on hard refresh.

Unauthenticated `/api/**` requests get a clean 401 via a scoped `HttpStatusEntryPoint` (the frontend's axios interceptor redirects to `/` on 401). [`/api/auth/providers`](../../backend/src/main/java/com/guavasoft/springbatch/dashboard/controller/AuthController.java) iterates the `ClientRegistrationRepository` so the login page can render one button per registered provider; per-button display (label/color/icon-url) is sourced from `app.oauth2.buttons.<registrationId>.*`.

`AuthProperties` (`app.auth.*`) controls provider attribute mapping (so non-GitHub providers work by remapping `login` / `name` / `avatar-url`) and an optional comma-delimited `allowed-logins` allow-list that rejects logins outside the list at OAuth2 user-loading time.

**Input sanitization for SQL.** Authentication is OAuth2-only — there are no passwords stored or hashed anywhere in this app, so the typical "use BCrypt" guidance doesn't apply. The remaining injection surface is custom JDBC SQL: always parameterize values via `NamedParameterJdbcTemplate` named binds (never concatenate user input), and build dynamic clauses (ORDER BY columns, etc.) from a whitelist `Map<String, String>` inside the repo impl, never from user input directly.

## Timestamps

API responses emit timestamps as ISO-8601 UTC instants (`2026-04-30T14:30:00Z`). [`TimestampFormatter`](../../backend/src/main/java/com/guavasoft/springbatch/dashboard/config/TimestampFormatter.java) is the single edge-conversion seam: it interprets a DB-local `LocalDateTime` in the active datasource's configured `timezone` (`app.datasources[*].timezone`, default `UTC`, parsed once at boot) and emits the UTC instant. Inject it wherever a timestamp leaves the API; never format locally. `JobRunMapper` wires it via `@Mapper(uses = TimestampFormatter.class)` so MapStruct picks it up automatically for any `LocalDateTime → String` mapping.

## Actuator

`spring-boot-starter-actuator` is wired up only for Kubernetes probes. [`application.yml`](../../backend/src/main/resources/application.yml) exposes only `health`, enables the readiness/liveness probe groups (`/actuator/health/{readiness,liveness}`), and **disables the auto DataSource health indicator** because the primary `DataSource` bean is a routing one that needs a per-request ThreadLocal — a status-time `getConnection()` would spuriously fail readiness. Don't re-enable it without working through that.

## Conventions

- Records, not Lombok-annotated classes, for response models.
- Don't write to `BATCH_*` tables — read-only.
- Native SQL only when JPA can't express it cleanly. Always parameterize values; build dynamic clauses from a whitelist; route engine-specific fragments through `SqlDialect`.
- Don't return JPA entities from controllers; map to a record first.
- Variable naming: prefer expressive names over abbreviations (`throughputBars` over `bars`, `stepDetails` over `details`). Single-letter / short names are fine for lambda parameters and generic type parameters.
- Use markdown link form for code references in docs/comments — IDE renders them clickable.
- Prefer editing existing files over creating new ones; don't add docs unless asked.
- When unsure of a project pattern, look in [`backend/AGENTS.md`](../../backend/AGENTS.md) before guessing.
