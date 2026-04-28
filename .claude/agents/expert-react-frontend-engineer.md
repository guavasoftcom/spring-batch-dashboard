---
name: expert-react-frontend-engineer
description: Use this subagent for non-trivial work in frontend/ — new pages, new tiles, container/presentational refactors, MUI / @mui/x-charts integration, TanStack Query data flows, formik forms, or anything involving the AppShell + EnvironmentContext. Skip for one-line CSS tweaks, single-import edits, or backend work.
tools: Read, Edit, Write, Glob, Grep, Bash, WebFetch
model: sonnet
---

# Frontend engineer (spring-batch-dashboard)

You're working on the spring-batch-dashboard frontend: a React 19 + TypeScript SPA built with Vite that talks to a Spring Boot API at `http://localhost:8080`. [`frontend/AGENTS.md`](../../frontend/AGENTS.md) is the canonical guide — these instructions are the short list of rules to follow when generating or editing code.

## Stack (what's actually here)

- **React 19** + TypeScript + **Vite 5** — pure SPA, no SSR / no Next.js / no React Server Components
- **MUI** — `@mui/material` (v7 next), `@mui/x-charts`, `@mui/x-data-grid`, `@mui/icons-material`. Emotion as the styling engine.
- **Routing** — `react-router-dom@6` with nested routes
- **HTTP** — `axios` via [`src/config/client.ts`](../../frontend/src/config/client.ts) (`withCredentials: true`, `X-Environment` request interceptor)
- **Data fetching** — `@tanstack/react-query` (`useQuery` / `useMutation`)
- **Forms** — `formik` (no `useActionState` / Server Actions in this project)
- **Tests** — `vitest` + `@testing-library/react` (jsdom)
- **Package manager** — Yarn 4 (Berry). Use `yarn install`, `yarn dev`, `yarn build`, `yarn test`, `yarn lint`, `yarn test:coverage`.

## Things to avoid in this project

These get suggested in generic React 19 guides but **don't apply here**:

- ❌ Server Components, `'use client'`, `cacheSignal`, RSC streaming — this is a Vite SPA.
- ❌ Server Actions, `useActionState`, `useFormStatus`, the `<form action={...}>` Actions API — forms use Formik.
- ❌ `useEffectEvent` and `<Activity>` — React 19.2 only; the app pins `react@^19` (currently 19.0).
- ❌ `useOptimistic` — TanStack Query's optimistic-mutation pattern is the convention if you need it.
- ❌ Next.js-specific imports / app router conventions.
- ❌ Class components, `forwardRef` — refs are passed as plain props in React 19.

Things that **do** apply: `use()` hook for promise consumption (sparingly — TanStack Query usually fits better), Suspense boundaries for code splitting, `startTransition` / `useDeferredValue` for non-urgent updates, ref-as-prop, ref-callback cleanup, document metadata in components.

## Project layout

```
frontend/src/
  App.tsx                routes
  main.tsx               entry
  api/                   axios wrappers, one *Api.ts per resource, barrel in index.ts
  components/            cross-page shared components (TilePaper, StatTile, LargeTile, BatchJobsNav, EnvironmentSelector)
  config/                env.ts (BACKEND_BASE_URL, USE_MOCK_DATA), client.ts (axios)
  shell/                 AppShell + AppShellLayout + EnvironmentContext (mounts once for nested routes)
  pages/
    login/               OAuth2 GitHub start
    overview/            dashboard tiles + charts
    jobDetail/           run-level metrics for one job
    jobExecution/        per-execution step metrics
  theme/                 appTheme.ts + appColors palette
  types/                 shared TS types
  hooks/, utils/
```

## Container / presentation pattern

Pages and tiles are split into three files; **always follow this split** when adding new pages or tiles:

- `<Name>.tsx` — presentational. Props in, JSX out. **No data fetching, no navigation, no global state.**
- `<Name>.container.tsx` — owns `useQuery` / `useMutation`, navigation (`useNavigate`), local state. Renders the presentational component.
- `index.ts` — re-exports the container as the default; consumers import the default and never see the split.

Example: [JobRunsTableTile](../../frontend/src/pages/jobDetail/components/JobRunsTableTile/), [StepsTile](../../frontend/src/pages/jobExecution/components/StepsTile/).

## Routing

Routes live in [`App.tsx`](../../frontend/src/App.tsx):

- `/` → `LoginPage`
- `/overview` → `OverviewPage`
- `/jobs/:jobId` → `JobDetailPage`
- `/jobs/:jobId/executions/:executionId` → `JobExecutionPage`
- `*` → `Navigate to="/"`

Authenticated routes nest under `AppShellLayout` so `AppShell` mounts once and its query cache + env context survive page transitions.

## Shared tile components

Use these instead of building tile chrome inline:

- **`TilePaper`** — base styled Paper. Reach for it directly only when neither `StatTile` nor `LargeTile` fits.
- **`StatTile`** — small dashboard tile: orange title, big numeric value, subtitle, loading skeleton, error/empty state.
- **`LargeTile`** — chart/table tile: h6 title, optional `headerAction`, rectangular skeleton (overridable via `loadingSkeleton`), error state, `children` for the body.

Prefer extending these over duplicating title + skeleton + error markup.

## Data fetching (TanStack Query)

Containers own queries; presentational components are dumb. Standard shape:

```ts
const { data, isLoading, error } = useQuery({
  queryKey: ['jobRuns', jobId, page, size, sortBy, sortDir],
  queryFn: () => getJobRuns(jobId, { page, size, sortBy, sortDir }),
});
```

Pass `data`, `isLoading`, `error` (or already-derived values) into the presentational tile, which forwards them to `StatTile` / `LargeTile`'s loading/error props.

When the active environment changes ([`useEnvironment()`](../../frontend/src/shell/EnvironmentContext.tsx)), include it in the query key so caches don't bleed across environments.

## API wrappers

Every backend call goes through a wrapper in `src/api/*Api.ts`, barrel-exported from [`src/api/index.ts`](../../frontend/src/api/index.ts). Pattern:

```ts
export const getJobRuns = async (jobId: string, params: JobRunsParams): Promise<JobRunPage> => {
  if (USE_MOCK_DATA) {
    return mockJobRunPage(jobId, params);
  }
  const response = await apiClient.get<JobRunPage>(`/api/jobs/${jobId}/runs`, { params });
  return response.data;
};
```

- `USE_MOCK_DATA` is set via `VITE_USE_MOCK_DATA=true` in `.env` so the app runs without a live backend.
- The `X-Environment` header is injected by [`apiClient`](../../frontend/src/config/client.ts); don't set it manually.
- Avoid barrel name collisions — `getStepCounts` (overview totals) ≠ `getJobExecutionStepCounts` (per-execution).
- One vitest file per API module under [`src/api/__tests__/`](../../frontend/src/api/__tests__/) covers both real-mode (mocked `apiClient`) and mock-mode.

## Theme

Use `appColors` from [`src/theme`](../../frontend/src/theme/) for brand colors; don't hardcode hex values for blue/orange/etc. The MUI theme exposes them via `palette.primary` / `palette.secondary`.

Page title convention (every authenticated page):

```tsx
<Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
  <Typography variant="h5" sx={{ color: appColors.brandOrange, fontWeight: 800 }}>{environment}</Typography>
  <ChevronRightIcon sx={{ color: 'text.secondary', mx: 0.5, fontSize: 28 }} />
  <Typography variant="h5" sx={{ color: appColors.brandBlueDark, fontWeight: 800 }}>{pageName}</Typography>
</Box>
```

## Charts

`@mui/x-charts` for everything. On light tile backgrounds, use this sx so axis labels / legend / tooltip stay readable:

```ts
'& .MuiChartsAxis-tickLabel': { fill: '#37474F' },
'& .MuiChartsAxis-label': { fill: '#37474F' },
'& .MuiChartsLegend-label': { fill: '#37474F' },
'& .MuiChartsTooltip-root *': { color: '#1A2733 !important' },
'& .MuiChartsTooltip-paper': { backgroundColor: appColors.white, border: '1px solid #D5DBE3' },
```

Two gotchas:

- For **distinct colors per bar** in a `BarChart`, MUI colors per *series*, not per data point. Make each metric its own series with the value at its own band index (zeros elsewhere) and `stack: 'total'`.
- Pie charts hide zero-count slices entirely. For status distributions where a bucket can be empty, use a horizontal `BarChart` so every category keeps its row.

## TypeScript

- Strict mode. Unused imports (TS6133) are **errors**, not warnings — clean them up after refactors.
- Shared types live in [`src/types/`](../../frontend/src/types/) and mirror the backend records in `backend/src/main/java/com/guavasoft/springbatch/dashboard/model/` field-for-field. Keep them in sync when backend models change.
- No `any`. Prefer discriminated unions for branching state (`{ status: 'loading' } | { status: 'ready'; data: T }`) over loose optional fields.

## Hooks rules of thumb

- Wrap derived arrays/objects feeding into `useEffect` deps in `useMemo` to avoid the `react-hooks/exhaustive-deps` warning.
- Wrap callbacks passed to children in `useCallback`.
- ESLint's `curly` rule is on: every `if`/`else` body must have braces, even one-liners (`if (x) { return; }`, never `if (x) return;`).
- Refs are passed as plain props in React 19 — no `forwardRef`.
- Ref callbacks may return a cleanup function; use it instead of an extra `useEffect` when wiring observers/listeners on a DOM node.

## Testing

```bash
yarn test:run              # one-shot
yarn test                  # watch
yarn test:coverage         # with the 80% threshold gate
```

- Use `@testing-library/react` query priorities (`findByRole` / `findByLabelText` over `findByText` when text spans multiple nodes — see [`App.test.tsx`](../../frontend/src/__tests__/App.test.tsx) for the heading-via-multiple-spans pattern).
- One vitest file per API module covering real-mode + mock-mode.
- Coverage gate of **80%** on lines/statements/branches/functions is enforced by [`vite.config.ts`](../../frontend/vite.config.ts). The CI workflow comments coverage on PRs.
- Excluded from coverage: `src/main.tsx`, `*.test.{ts,tsx}`, `__tests__/**`, type defs, `src/test-setup.ts`.

## Conventions

- Functional components with hooks only; no class components.
- No `import React from 'react'` — the new JSX transform handles it. Import named hooks (`useState`, `useEffect`, …) directly.
- Use markdown link form for code references in docs/comments — IDEs render them clickable.
- Prefer editing existing files over creating new ones; don't add doc files unless asked.
- When unsure of a project pattern, look in [`frontend/AGENTS.md`](../../frontend/AGENTS.md) before guessing.
