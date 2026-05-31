/**
 * NexusAuth — API Service Layer
 * All calls go through the BFF Gateway at https://localhost:8080
 * credentials: "include" is set on EVERY request for session cookie propagation
 */

const GATEWAY = '';

// ─── Core fetch wrapper ────────────────────────────────────────────────────

async function apiFetch(path, options = {}) {
  const url = `${GATEWAY}${path}`;

  const response = await fetch(url, {
    credentials: 'include',           // Always send/receive session cookies
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      ...(options.headers || {}),
    },
    ...options,
  });

  return response;
}

// ─── Auth Endpoints ────────────────────────────────────────────────────────

/**
 * POST /api/auth/login
 * Returns: { status: "AUTHENTICATED" | "AWAITING_MFA", user?: {...} }
 */
export async function loginWithCredentials(username, password) {
  const response = await apiFetch('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ login: username, password }), // Maps properly to Auth Service's LoginRequest (login & password)
  });

  const data = await response.json().catch(() => ({}));
  return { status: response.status, data };
}

/**
 * POST /api/auth/register
 * Body: { username, email, password }
 */
export async function register(username, email, password) {
  const response = await apiFetch('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify({ username, email, password }),
  });

  const data = await response.json().catch(() => ({}));
  return { status: response.status, data };
}

/**
 * POST /api/auth/2fa/verify
 * Body: { methodType: "TOTP", code }
 * Returns: { status: "VERIFIED" }
 */
export async function verifyMfa(code) {
  const response = await apiFetch('/api/auth/2fa/verify', {
    method: 'POST',
    body: JSON.stringify({ methodType: 'TOTP', code }),
  });

  const data = await response.json().catch(() => ({}));
  return { status: response.status, data };
}

/**
 * POST /api/auth/2fa/setup
 * Body: { methodType: "TOTP" }
 * Returns: { status: "SETUP_INITIATED", setupDetails: { qrCodeUrl, secret } }
 */
export async function getMfaSetup() {
  const response = await apiFetch('/api/auth/2fa/setup', {
    method: 'POST',
    body: JSON.stringify({ methodType: 'TOTP' }),
  });
  const data = await response.json().catch(() => ({}));
  return { status: response.status, data };
}

/**
 * POST /api/auth/2fa/enable
 * Body: { methodType: "TOTP", code }
 * Returns: { status: "ENABLED" }
 */
export async function confirmMfaSetup(code) {
  const response = await apiFetch('/api/auth/2fa/enable', {
    method: 'POST',
    body: JSON.stringify({ methodType: 'TOTP', code }),
  });

  const data = await response.json().catch(() => ({}));
  return { status: response.status, data };
}

/**
 * GET /api/auth/users — guarded endpoint (tests auth status)
 */
export async function getUsers() {
  const response = await apiFetch('/api/auth/users');
  
  // Handles plain text responses since auth-service /users returns a raw text string
  const contentType = response.headers.get('content-type') || '';
  let data;
  if (contentType.includes('application/json')) {
    data = await response.json().catch(() => ({}));
  } else {
    data = await response.text().catch(() => '');
  }
  
  return { status: response.status, data };
}

// ─── OAuth2 ────────────────────────────────────────────────────────────────

/**
 * Initiates Google OAuth2 by doing a full browser redirect.
 * The BFF gateway will handle the OAuth dance and redirect back.
 */
export function redirectToGoogleOAuth(isLink = false) {
  const query = isLink ? '?link=true' : '';
  window.location.href = `${GATEWAY}/oauth2/authorization/google${query}`;
}

/**
 * Initiates FortyTwo OAuth2 by doing a full browser redirect.
 */
export function redirectToFortyTwoOAuth(isLink = false) {
  const query = isLink ? '?link=true' : '';
  window.location.href = `${GATEWAY}/oauth2/authorization/fortytwo${query}`;
}

/**
 * POST /api/auth/oauth2/unlink
 * Body: { provider }
 */
export async function unlinkOAuth2Provider(provider) {
  const response = await apiFetch('/api/auth/oauth2/unlink', {
    method: 'POST',
    body: JSON.stringify({ provider }),
  });

  const data = await response.json().catch(() => ({}));
  return { status: response.status, data };
}

/**
 * POST /api/auth/logout
 * Triggers full stateful session invalidation on the BFF Gateway.
 */
export async function logoutUser() {
  const response = await apiFetch('/api/auth/logout', {
    method: 'POST',
  });
  return response.status;
}

// ─── Session Interceptor ──────────────────────────────────────────────────

/**
 * Wraps an API call and handles session-based interceptor logic:
 * - 401 → callback for redirect to login
 * - 403 → callback for redirect to MFA challenge
 */
export async function withSessionIntercept(apiFn, { on401, on403 } = {}) {
  try {
    const result = await apiFn();

    if (result.status === 401) {
      on401?.();
      return null;
    }

    if (result.status === 403) {
      on403?.();
      return null;
    }

    return result;
  } catch (error) {
    console.error('[API] Network error:', error);
    throw error;
  }
}
