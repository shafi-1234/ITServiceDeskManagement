export const API = import.meta.env.VITE_API_URL || 'http://localhost:1414';

async function call(path, options = {}) {
  const res = await fetch(`${API}${path}`, {
    credentials: 'include', // send the session cookie set after Google login
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });
  if (res.status === 401) throw Object.assign(new Error('Unauthorized'), { status: 401 });
  if (!res.ok) {
    let msg = `Request failed (${res.status})`;
    try { const b = await res.json(); msg = b.message || b.error || msg; } catch { /* ignore */ }
    throw Object.assign(new Error(msg), { status: res.status });
  }
  return res.status === 204 ? null : res.json();
}

export const loginUrl = `${API}/oauth2/authorization/google`;

export const api = {
  me: () => call('/api/me'),
  register: (body) => call('/api/auth/register', { method: 'POST', body: JSON.stringify(body) }),
  login: async (username, password) => {
    const res = await fetch(`${API}/api/login`, {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({ username, password }),
    });
    if (!res.ok) throw new Error('Invalid username or password');
  },
  logout: () => call('/api/logout', { method: 'POST' }),

  myRequests: () => call('/api/requests'),
  myStats: () => call('/api/requests/stats'),
  create: (body) => call('/api/requests', { method: 'POST', body: JSON.stringify(body) }),
  update: (id, body) => call(`/api/requests/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
  remove: (id) => call(`/api/requests/${id}`, { method: 'DELETE' }),
  reopen: (id) => call(`/api/requests/${id}/reopen`, { method: 'POST' }),

  allRequests: (status) => call(`/api/admin/requests${status ? `?status=${status}` : ''}`),
  allStats: () => call('/api/admin/stats'),
  setStatus: (id, status, comment) =>
    call(`/api/admin/requests/${id}/status`, { method: 'PUT', body: JSON.stringify({ status, comment }) }),
};
