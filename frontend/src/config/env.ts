// Empty base URL means "same origin": every request is issued as a relative path.
// That is what the released image needs — Spring Boot serves the SPA and the API from
// the same host:port, so the dashboard works under whatever domain it is deployed to
// without baking a URL into the bundle at build time. Dev overrides it via
// .env.development because Vite serves on :5173 while the backend runs on :8080.
export const BACKEND_BASE_URL = import.meta.env.VITE_BACKEND_BASE_URL ?? '';

export const USE_MOCK_DATA = import.meta.env.VITE_USE_MOCK_DATA === 'true';
