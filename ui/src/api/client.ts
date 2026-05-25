import axios, { AxiosError, AxiosInstance, InternalAxiosRequestConfig } from 'axios';

const apiClient: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    // Don't redirect on 401 in interceptor
    // Let router guard and auth composable handle it gracefully
    // This prevents loops during initial auth check
    return Promise.reject(error);
  }
);

export default apiClient;
