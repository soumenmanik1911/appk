import { neon } from '@neondatabase/serverless';

const MAX_BODY_BYTES = 200_000;
const MAX_NOTIFS_PER_SYNC = 500;
const MAX_ACTIONS_PER_SYNC = 500;
const MAX_ROWS_PER_DEVICE = 1000;
const RATE_LIMIT_WINDOW_MS = 60_000;
const RATE_LIMIT_MAX = 60;
const REGISTER_RATE_LIMIT_MAX = 10;

const rateLimits = new Map(); // ip -> { count, resetAt }
const registerRateLimits = new Map(); // ip -> { count, resetAt }

function jsonBody(req) {
  return req.json().catch(() => null);
}

function isValidKey(k) {
  return /^[a-z0-9_|:.]+$/.test(k) && k.length <= 200;
}

async function hashToken(token) {
  const buf = await crypto.subtle.digest('SHA-256', new TextEncoder().encode(token));
  const arr = new Uint8Array(buf);
  return Array.from(arr, b => b.toString(16).padStart(2, '0')).join('');
}

function checkRateLimit(ip, map = rateLimits, max = RATE_LIMIT_MAX) {
  const now = Date.now();
  const entry = map.get(ip);
  if (!entry || now > entry.resetAt) {
    map.set(ip, { count: 1, resetAt: now + RATE_LIMIT_WINDOW_MS });
    return true;
  }
  if (entry.count >= max) {
    return false;
  }
  entry.count++;
  return true;
}

export default {
  async fetch(request, env, ctx) {
    const url = new URL(request.url);
    const path = url.pathname;
    const ip = request.headers.get('CF-Connecting-IP') || 'unknown';

    if (!checkRateLimit(ip)) {
      return jsonResponse({ error: 'rate limited' }, 429);
    }

    // CORS preflight
    if (request.method === 'OPTIONS') {
      return new Response(null, {
        status: 204,
        headers: {
          'Access-Control-Allow-Origin': '*',
          'Access-Control-Allow-Methods': 'GET, POST, DELETE, OPTIONS',
          'Access-Control-Allow-Headers': 'Content-Type, Authorization, X-Reg-Secret',
        },
      });
    }

    // Health check - no auth
    if (path === '/health' && request.method === 'GET') {
      return jsonResponse({ ok: true });
    }

    // Auth: bearer token
    let deviceId = null;
    const auth = request.headers.get('Authorization');
    if (auth && auth.startsWith('Bearer ')) {
      const token = auth.slice(7);
      const tokenHash = await hashToken(token);
      const sql = neon(env.NEON_DATABASE_URL);
      const rows = await sql`SELECT device_id FROM devices WHERE token_hash = ${tokenHash}`;
      if (rows.length > 0) {
        deviceId = rows[0].device_id;
      }
    }

    // Register: requires X-Reg-Secret header
    if (path === '/register' && request.method === 'POST') {
      const regSecret = request.headers.get('X-Reg-Secret');
      if (regSecret !== env.REG_SECRET) {
        return jsonResponse({ error: 'forbidden' }, 403);
      }
      // Per-IP rate limit on /register
      if (!checkRateLimit(ip, registerRateLimits, REGISTER_RATE_LIMIT_MAX)) {
        return jsonResponse({ error: 'rate limited' }, 429);
      }
      return handleRegister(request, env);
    }

    // All other endpoints require auth
    if (!deviceId) {
      return jsonResponse({ error: 'unauthorized' }, 401);
    }

    if (path === '/sync' && request.method === 'POST') {
      return handleSync(request, env, deviceId);
    }

    if (path === '/me' && request.method === 'DELETE') {
      return handleDeleteMe(env, deviceId);
    }

    return jsonResponse({ error: 'not found' }, 404);
  },
};

async function handleRegister(request, env) {
  let body;
  try {
    body = await request.json();
  } catch {
    return jsonResponse({ error: 'invalid json' }, 400);
  }

  const { token } = body;
  if (!token || typeof token !== 'string') {
    return jsonResponse({ error: 'token required' }, 400);
  }

  const deviceId = 'dev_' + crypto.randomUUID();
  const tokenHash = await hashToken(token);

  const sql = neon(env.NEON_DATABASE_URL);
  await sql`
    INSERT INTO devices (device_id, token_hash)
    VALUES (${deviceId}, ${tokenHash})
    ON CONFLICT (token_hash) DO UPDATE SET device_id = EXCLUDED.device_id
  `;

  return jsonResponse({ device_id: deviceId });
}

async function handleSync(request, env, deviceId) {
  const contentLength = request.headers.get('Content-Length');
  if (contentLength && parseInt(contentLength) > MAX_BODY_BYTES) {
    return jsonResponse({ error: 'body too large' }, 413);
  }

  let body;
  try {
    body = await request.json();
  } catch {
    return jsonResponse({ error: 'invalid json' }, 400);
  }

  const { notifs = [], actions = [] } = body;

  if (!Array.isArray(notifs) || notifs.length > MAX_NOTIFS_PER_SYNC) {
    return jsonResponse({ error: 'invalid notifs' }, 400);
  }
  if (!Array.isArray(actions) || actions.length > MAX_ACTIONS_PER_SYNC) {
    return jsonResponse({ error: 'invalid actions' }, 400);
  }

  for (const n of notifs) {
    if (!isValidKey(n.pkg) || !isValidKey(n.category)) {
      return jsonResponse({ error: 'invalid field' }, 400);
    }
    if (typeof n.notif_id !== 'number' || typeof n.posted_at !== 'number') {
      return jsonResponse({ error: 'invalid field' }, 400);
    }
    if (!n.local_id || typeof n.local_id !== 'string') {
      return jsonResponse({ error: 'local_id required' }, 400);
    }
    const allowedNotif = ['notif_id', 'device_id', 'pkg', 'category', 'posted_at', 'status', 'local_id'];
    for (const k of Object.keys(n)) {
      if (!allowedNotif.includes(k)) {
        return jsonResponse({ error: `unknown field: ${k}` }, 400);
      }
    }
  }

  for (const a of actions) {
    if (typeof a.notif_id !== 'number') {
      return jsonResponse({ error: 'invalid action field' }, 400);
    }
    if (!a.local_id || typeof a.local_id !== 'string') {
      return jsonResponse({ error: 'local_id required' }, 400);
    }
    const allowedAction = ['notif_id', 'device_id', 'choice', 'delay_min', 'hour', 'weekday', 'opened_after', 'suggested_choice', 'local_id'];
    for (const k of Object.keys(a)) {
      if (!allowedAction.includes(k)) {
        return jsonResponse({ error: `unknown action field: ${k}` }, 400);
      }
    }
  }

  const sql = neon(env.NEON_DATABASE_URL);

  // Per-device row cap
  const rowCount = await sql`SELECT count(*) as c FROM (
    SELECT 1 FROM notifs WHERE device_id = ${deviceId}
    UNION ALL
    SELECT 1 FROM actions WHERE device_id = ${deviceId}
  ) t`;
  if (parseInt(rowCount[0].c) >= MAX_ROWS_PER_DEVICE) {
    return jsonResponse({ error: 'device row cap reached' }, 403);
  }

  for (const n of notifs) {
    await sql`
      INSERT INTO notifs (notif_id, device_id, pkg, category, posted_at, status, local_id)
      VALUES (${n.notif_id}, ${deviceId}, ${n.pkg}, ${n.category}, ${n.posted_at}, ${n.status || 'pending'}, ${n.local_id})
      ON CONFLICT (device_id, local_id) DO NOTHING
    `;
  }

  for (const a of actions) {
    await sql`
      INSERT INTO actions (notif_id, device_id, choice, delay_min, hour, weekday, opened_after, suggested_choice, local_id)
      VALUES (${a.notif_id}, ${deviceId}, ${a.choice}, ${a.delay_min ?? null}, ${a.hour ?? null}, ${a.weekday ?? null}, ${a.opened_after ?? null}, ${a.suggested_choice ?? null}, ${a.local_id})
      ON CONFLICT (device_id, local_id) DO NOTHING
    `;
  }

  return jsonResponse({ synced: { notifs: notifs.length, actions: actions.length } });
}

async function handleDeleteMe(env, deviceId) {
  const sql = neon(env.NEON_DATABASE_URL);
  await sql`DELETE FROM devices WHERE device_id = ${deviceId}`;
  return jsonResponse({ deleted: true });
}

function jsonResponse(data, status = 200) {
  return new Response(JSON.stringify(data), {
    status,
    headers: {
      'Content-Type': 'application/json',
      'Access-Control-Allow-Origin': '*',
    },
  });
}
