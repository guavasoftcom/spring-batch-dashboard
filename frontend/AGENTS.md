# Frontend agent guide

React + TypeScript + Vite app for the Spring Batch Dashboard. Backend is a Spring Boot API at `http://localhost:8080`; this app runs at `http://localhost:5173`.

## Stack

- React 19, TypeScript, Vite 5
- MUI: `@mui/material` (next), `@mui/x-charts`, `@mui/icons-material`
- Routing: `react-router-dom`
- HTTP: `axios` via `src/config/client.ts` (`withCredentials: true` for the OAuth2 session cookie; injects `X-Environment` header from localStorage)
- Data fetching: `@tanstack/react-query`
- Forms: `formik`
- Tests: `vitest` + `@testing-library/react` (jsdom)
- Package manager: `yarn@4` (Berry). Run `yarn install`, `yarn dev`, `yarn build`, `yarn test`, `yarn lint`.

## Layout

```
src/
  App.tsx                   routes
  main.tsx                  entry
  api/
    index.ts                barrel
    client.ts               (re-exported from config/client.ts)
    authApi.ts              getCurrentUser, logout
    environmentApi.ts       getEnvironments
    jobsApi.ts              getJobs
    jobRunsApi.ts           run-level metrics + paginated runs
    jobExecutionStepsApi.ts per-execution step metrics
    dashboardApi.ts         overview tiles
    __tests__/              one *.test.ts per api module
  components/               shared cross-page components
    BatchJobsNav/
    EnvironmentSelector/
    TilePaper/              base styled Paper for tiles
    StatTile/               title + big value + subtitle (loading/error/empty)
    LargeTile/               title + optional headerAction + content (loading/error overrides)
  config/
    env.ts                  BACKEND_BASE_URL, LOGIN_URL, USE_MOCK_DATA
    client.ts               axios instance with X-Environment interceptor
  shell/                    app shell (formerly pages/shared/)
    AppShell.tsx            AppBar + sidebar; mounts once via AppShellLayout
    AppShellLayout.tsx      <Outlet/> wrapper for nested routes
    EnvironmentContext.tsx  global environment selection (persisted to localStorage)
  pages/
    login/                  LoginPage (OAuth2 GitHub start)
    overview/               OverviewPage (dashboard tiles + charts)
    jobDetail/              JobDetailPage (run-level tiles + JobRunsTable)
    jobExecution/           JobExecutionPage (per-execution tiles + StepsTable)
  theme/                    appTheme.ts + appColors palette
  types/                    shared TS types
  hooks/, utils/
```

Each page directory has `<Name>.tsx` (presentational), `<Name>.container.tsx` (data + state), and `index.ts` re-exporting the container. Tiles inside a page follow the same split: `Tile.tsx`, `Tile.container.tsx`, `index.ts` re-exports the container.

## Routing

Routes in [App.tsx](src/App.tsx):

- `/` → `LoginPage`
- `/overview` → `OverviewPage`
- `/jobs/:jobId` → `JobDetailPage` (runs for that job)
- `/jobs/:jobId/executions/:executionId` → `JobExecutionPage` (steps for that run)
- `*` → `Navigate to="/"`

Authenticated routes are nested under `AppShellLayout` so `AppShell` mounts once and its state (current user, env context, query cache) persists across navigations.

## Container / presentation pattern

Pages and tiles split into:

- `<Name>.tsx` — presentational; props in, JSX out; no fetching, no navigation.
- `<Name>.container.tsx` — owns `useQuery`, navigation, local UI state; renders the presentational component.
- `index.ts` — re-exports the container as default so consumers don't see the split.

Examples: [JobRunsTableTile](src/pages/jobDetail/components/JobRunsTableTile/), [StepsTile](src/pages/jobExecution/components/StepsTile/).

## Shared tile components

Use the shared components in `src/components/` instead of building tile chrome inline:

- **`TilePaper`** — base styled Paper. Use directly only when neither `StatTile` nor `LargeTile` fits.
- **`StatTile`** — orange title, big numeric value, subtitle, loading skeletons, error message, and optional empty-state. Used for the small dashboard tiles (Job Executions, Runtime, Throughput, Total Runs, Success Rate, Avg Duration, etc.).
- **`LargeTile`** — h6 title, optional `headerAction` (right-aligned), rectangular skeleton (overridable via `loadingSkeleton`), error message, and `children` for the data body. Used for charts and tables (JobStatusChartTile, ProcessingMetricsTile, RunDurationTrendTile, JobRunsTableTile, StepsTableTile).

When writing a new tile, prefer extending `StatTile` / `LargeTile` over duplicating the title + skeleton + error markup. Reach for `TilePaper` directly only when neither fits the pattern (e.g. small chart with body2 title — see `StepDurationsTile`).

## Authenticated page pattern

`AppShell` ([src/shell/AppShell.tsx](src/shell/AppShell.tsx)) provides:

- AppBar with logo, current user avatar (initials fallback), Logout button
- Left sidebar with `EnvironmentSelector` + `BatchJobsNav` (active item derived from `useParams().jobId`)
- Footer
- `EnvironmentContext.Provider` (persisted to `localStorage` under `spring-batch-dashboard.environment`)
- Scrollable content area; pages render their own `Container` inside it

`useEnvironment()` works from any authenticated page since they all render under `AppShell`'s provider. Selected environment is also automatically forwarded to the backend on every request via the `X-Environment` header (interceptor in [client.ts](src/config/client.ts)).

## Page title convention

Each page renders a breadcrumb-style title. Environment in `appColors.brandOrange`, separated by `<ChevronRightIcon />` from the rest in `appColors.brandBlueDark`. All segments `variant="h5"`, `fontWeight: 800`.

```tsx
<Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
  <Typography variant="h5" sx={{ color: appColors.brandOrange, fontWeight: 800 }}>{environment}</Typography>
  <ChevronRightIcon sx={{ color: 'text.secondary', mx: 0.5, fontSize: 28 }} />
  <Typography variant="h5" sx={{ color: appColors.brandBlueDark, fontWeight: 800 }}>{pageName}</Typography>
</Box>
```

For clickable segments use `MuiLink` with matching font sizing — see [JobExecutionPage](src/pages/jobExecution/JobExecutionPage.tsx).

## Charts

`@mui/x-charts` for everything. On light tile backgrounds use this sx for axes/legend/tooltip:

```ts
'& .MuiChartsAxis-tickLabel': { fill: '#37474F' },
'& .MuiChartsAxis-label': { fill: '#37474F' },
'& .MuiChartsLegend-label': { fill: '#37474F' },
'& .MuiChartsTooltip-root *': { color: '#1A2733 !important' },
'& .MuiChartsTooltip-paper': { backgroundColor: appColors.white, border: '1px solid #D5DBE3' },
```

To get **distinct colors per bar** in a `BarChart`, MUI colors per *series*, not per data point — make each metric its own series with the value at its own band index (zeros elsewhere) and `stack: 'total'`.

Pie charts hide zero-count slices entirely; for status distributions where a bucket can be empty, use a horizontal `BarChart` instead so every category keeps its labeled row.

## API

- `apiClient` lives in [src/config/client.ts](src/config/client.ts); axios instance with `withCredentials: true` and an `X-Environment` request interceptor.
- Endpoint wrappers live in `src/api/*Api.ts`; barrel-exported from [src/api/index.ts](src/api/index.ts). Each function follows the pattern:

```ts
export const getX = async (...): Promise<T> => {
  if (USE_MOCK_DATA) {
    return /* derived from local seed data */;
  }
  const response = await apiClient.get<T>(path, { params });
  return response.data;
};
```

- Set `VITE_USE_MOCK_DATA=true` in `.env` to run the app without a live backend; every endpoint returns canned data and `apiClient` is bypassed.
- Avoid name collisions in the barrel — `getStepCounts` (overview totals) is distinct from `getJobExecutionStepCounts` (per-execution).
- One Vitest file per API module under [src/api/__tests__/](src/api/__tests__/) covers both real-mode (asserts URL/params via mocked `apiClient`) and mock-mode (asserts canned data, no HTTP).

## Theme

Use `appColors` from `src/theme` for brand colors (don't hardcode hex values for blue/orange/etc.). MUI's theme also exposes them via `palette.primary` / `palette.secondary`.

## Conventions

- Use markdown link form for code references in docs/comments — IDE renders them clickable.
- ESLint `curly` rule is enforced: every `if`/`else` body must have braces, even one-liners (`if (x) { return; }`, not `if (x) return;`).
- Unused TS imports (TS6133) are errors, not warnings — clean them up after refactors.
- Prefer `Edit` over `Write` when modifying existing files.
- Don't add documentation files unless asked.
- Wrap derived arrays/objects feeding into `useEffect` deps in `useMemo` (avoid the `react-hooks/exhaustive-deps` warning); wrap callbacks passed to children in `useCallback`.
