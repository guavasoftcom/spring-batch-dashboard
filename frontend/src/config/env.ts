const backendBaseUrl = import.meta.env.VITE_BACKEND_BASE_URL;

export const BACKEND_BASE_URL = backendBaseUrl && backendBaseUrl.length > 0 ? backendBaseUrl : 'http://localhost:8080';

export const USE_MOCK_DATA = import.meta.env.VITE_USE_MOCK_DATA === 'true';
