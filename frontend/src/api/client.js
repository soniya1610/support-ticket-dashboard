const BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/+$/, '');

/** Error thrown for every failed API call, carrying the backend's error payload when there is one. */
export class ApiError extends Error {
  constructor(message, { status = 0, code = 'UNKNOWN', details = [] } = {}) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.code = code;
    this.details = details;
    // field -> first message, handy for forms
    this.fieldErrors = {};
    for (const d of details || []) {
      if (d.field && !this.fieldErrors[d.field]) this.fieldErrors[d.field] = d.message;
    }
  }
}

async function request(path, { method = 'GET', body, signal } = {}) {
  let response;
  try {
    response = await fetch(`${BASE_URL}${path}`, {
      method,
      headers: body !== undefined ? { 'Content-Type': 'application/json' } : undefined,
      body: body !== undefined ? JSON.stringify(body) : undefined,
      signal,
    });
  } catch (err) {
    if (err.name === 'AbortError') throw err;
    throw new ApiError('Unable to reach the server. Check that the backend is running.', { code: 'NETWORK_ERROR' });
  }

  if (!response.ok) {
    let payload = null;
    try {
      payload = await response.json();
    } catch {
      /* body was not JSON */
    }
    const error = payload && payload.error;
    throw new ApiError((error && error.message) || `Request failed (HTTP ${response.status})`, {
      status: response.status,
      code: error && error.code,
      details: (error && error.details) || [],
    });
  }
  return response.json();
}

function toQuery(params) {
  const query = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') query.set(key, String(value));
  });
  const s = query.toString();
  return s ? `?${s}` : '';
}

export function listTickets(params, signal) {
  return request(`/api/tickets${toQuery(params)}`, { signal });
}

export function getTicket(id, signal) {
  return request(`/api/tickets/${encodeURIComponent(id)}`, { signal });
}

export function createTicket(values) {
  return request('/api/tickets', { method: 'POST', body: values });
}

export function updateTicket(id, changes) {
  return request(`/api/tickets/${encodeURIComponent(id)}`, { method: 'PATCH', body: changes });
}

export function getSummary(signal) {
  return request('/api/tickets/summary', { signal });
}
