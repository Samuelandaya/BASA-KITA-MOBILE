// COPIED from the website (src/api/auth.js) by tools/sync-web.mjs. Edit it there, then run the script again.
// Account service. The backend decides each account's role ('learner' or 'admin');
// the app only reads `user.role` and opens the matching dashboard.
//
// Every function calls the backend when VITE_API_URL is set (see client.js) and
// otherwise answers from demo stand-ins. Expected endpoints (all JSON):
//
//   POST /auth/login           { email, password }          -> { user }
//   POST /auth/signup          { name, username, email, password } -> { user }   (role: 'pending')
//   POST /auth/verify          { email, code }              -> { user }   (role: 'learner')
//   POST /auth/resend-code     { email }                    -> {}
//   POST /auth/forgot-password { email }                    -> {}         (always succeeds)
//   POST /auth/reset-password  { token, password }          -> {}
//
//   user = { firstName, lastName, username, email, role: 'learner' | 'admin' }
//   Errors: a non-2xx status with { message } that is safe to show to the user.
import { apiConnected, pause, request } from './client.js';

// Demo stand-ins until the backend exists:
// - admin@basakita.ph signs in as an admin
// - sam@basakita.ph is a returning learner with sample reading history
// - any other email is a brand-new learner with no history
const DEMO_ADMIN_EMAILS = ['admin@basakita.ph'];
const DEMO_RETURNING_EMAILS = ['sam@basakita.ph'];

export async function signIn(email, password) {
  const cleanEmail = email.trim().toLowerCase();
  if (!cleanEmail || !password) throw new Error('Please enter your email and password.');
  if (!/^\S+@\S+\.\S+$/.test(cleanEmail)) throw new Error('Please enter a valid email address.');
  if (apiConnected) return (await request('/auth/login', { body: { email: cleanEmail, password } })).user;
  await pause();
  const isAdmin = DEMO_ADMIN_EMAILS.includes(cleanEmail);
  const handle = cleanEmail.split('@')[0];
  return isAdmin
    ? { firstName: 'Ana', lastName: 'Reyes', username: handle, email: cleanEmail, role: 'admin' }
    : DEMO_RETURNING_EMAILS.includes(cleanEmail)
      ? { firstName: 'Sam', lastName: 'Dela Cruz', username: handle, email: cleanEmail, role: 'learner', returning: true }
      : { firstName: niceName(handle), lastName: '', username: handle, email: cleanEmail, role: 'learner' };
}

const niceName = (handle) => {
  const first = handle.split(/[._-]/)[0] || 'Reader';
  return first.charAt(0).toUpperCase() + first.slice(1);
};

export const homeFor = (user) => (user?.role === 'admin' ? '/admin' : '/app');

export async function signUp({ name, username, email, password }) {
  if (apiConnected) return (await request('/auth/signup', { body: { name, username, email, password } })).user;
  await pause();
  const [firstName, ...rest] = name.trim().split(/\s+/);
  return { firstName, lastName: rest.join(' '), username, email: email.trim().toLowerCase(), role: 'pending' };
}

// Demo: any 6-digit code works.
export async function verifyEmail(user, code) {
  if (apiConnected) return (await request('/auth/verify', { body: { email: user.email, code } })).user;
  await pause();
  return { ...user, role: 'learner' };
}

export async function resendCode(email) {
  if (apiConnected) return request('/auth/resend-code', { body: { email } });
  await pause();
}

export async function requestPasswordReset(email) {
  if (apiConnected) return request('/auth/forgot-password', { body: { email } });
  await pause();
}

// `token` comes from the link in the reset email (?token=...).
export async function resetPassword(token, password) {
  if (apiConnected) return request('/auth/reset-password', { body: { token, password } });
  await pause();
}
