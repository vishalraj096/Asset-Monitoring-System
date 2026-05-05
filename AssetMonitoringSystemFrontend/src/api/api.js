import axios from 'axios';

const API = axios.create({
  baseURL: '/api',
});

API.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

API.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

/**
 * Extract a human-readable error message from an API error response.
 * Handles validation errors (field-level) and generic error messages.
 */
export function extractErrorMessage(err, fallback = 'Something went wrong') {
  const data = err.response?.data;
  if (!data) return fallback;

  // If there are field-level validation errors, join them
  if (data.errors && typeof data.errors === 'object') {
    const msgs = Object.values(data.errors);
    if (msgs.length > 0) return msgs.join('. ');
  }

  return data.message || fallback;
}

export default API;
