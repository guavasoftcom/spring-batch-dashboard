import axios from 'axios';
import { BACKEND_BASE_URL } from './env';

export const apiClient = axios.create({
  baseURL: BACKEND_BASE_URL,
  withCredentials: true,
});

const ENV_STORAGE_KEY = 'spring-batch-dashboard.environment';

apiClient.interceptors.request.use((config) => {
  const env = typeof window !== 'undefined' ? window.localStorage.getItem(ENV_STORAGE_KEY) : null;
  if (env) {
    config.headers.set('X-Environment', env);
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (
      error?.response?.status === 401
      && typeof window !== 'undefined'
      && window.location.pathname !== '/'
    ) {
      window.location.replace('/');
    }
    return Promise.reject(error);
  },
);
