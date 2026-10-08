// The app's own version of the website's src/api/client.js (NOT copied by tools/sync-web.mjs),
// so the copied api/auth.js works unchanged on the phone.
//
// Set EXPO_PUBLIC_API_URL (for example in .env.local) to turn the real backend on:
//
//   EXPO_PUBLIC_API_URL=https://api.example.com
//
// While it is empty the app runs in demo mode, exactly like the website.
export const API_URL = (process.env.EXPO_PUBLIC_API_URL || '').replace(/\/$/, '');
export const apiConnected = Boolean(API_URL);

// JSON request. Throws an Error with the backend's `message` when the response is not OK.
export async function request(path, { method = 'POST', body } = {}) {
  const res = await fetch(`${API_URL}${path}`, {
    method,
    credentials: 'include',
    headers: body ? { 'Content-Type': 'application/json' } : undefined,
    body: body ? JSON.stringify(body) : undefined,
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(data.message || 'Something went wrong. Please try again.');
  return data;
}

export const pause = (ms = 300) => new Promise((r) => setTimeout(r, ms));
