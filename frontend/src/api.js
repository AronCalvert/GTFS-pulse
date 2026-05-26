export const API = 'http://localhost:8080/api/v1';

const TOKEN_KEY = 'gtfspulse_token';

export const getToken = () => localStorage.getItem(TOKEN_KEY);
export const setToken = (t) => localStorage.setItem(TOKEN_KEY, t);
export const clearToken = () => localStorage.removeItem(TOKEN_KEY);

export function isTokenExpired(token) {
  try {
    const { exp } = JSON.parse(atob(token.split('.')[1]));
    return Date.now() / 1000 >= exp;
  } catch {
    return true;
  }
}

export async function apiFetch(path, options = {}) {
  const token = getToken();
  const headers = { 'Content-Type': 'application/json', ...options.headers };
  if (token) headers['Authorization'] = `Bearer ${token}`;
  const res = await fetch(`${API}${path}`, { ...options, headers });
  if (res.status === 401) {
    document.dispatchEvent(new CustomEvent('auth:unauthorized'));
    throw new Error('401 Unauthorized');
  }
  if (!res.ok) throw new Error(`${res.status} ${res.statusText}`);
  const text = await res.text();
  return text ? JSON.parse(text) : null;
}
