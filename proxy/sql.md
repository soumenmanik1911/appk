# Run this SQL in the Neon console SQL Editor (https://console.neon.tech)
# Create a database first, then paste this into the SQL editor.

CREATE TABLE IF NOT EXISTS devices (
    device_id TEXT PRIMARY KEY,
    token_hash TEXT NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS notifs (
    notif_id INTEGER NOT NULL,
    device_id TEXT NOT NULL REFERENCES devices(device_id) ON DELETE CASCADE,
    local_id TEXT NOT NULL,
    pkg TEXT NOT NULL,
    category TEXT NOT NULL,
    posted_at BIGINT NOT NULL,
    status TEXT NOT NULL DEFAULT 'pending',
    synced_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (device_id, notif_id),
    UNIQUE (device_id, local_id)
);

-- NOTE: no text column -- raw notification text never leaves the phone

CREATE TABLE IF NOT EXISTS actions (
    notif_id INTEGER NOT NULL,
    device_id TEXT NOT NULL REFERENCES devices(device_id) ON DELETE CASCADE,
    local_id TEXT NOT NULL,
    choice TEXT NOT NULL,
    delay_min INTEGER,
    hour INTEGER,
    weekday INTEGER,
    opened_after BOOLEAN,
    suggested_choice TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (device_id, notif_id),
    UNIQUE (device_id, local_id)
);

CREATE TABLE IF NOT EXISTS decisions (
    key TEXT PRIMARY KEY,
    delay_min INTEGER NOT NULL,
    reason TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ NOT NULL DEFAULT (NOW() + INTERVAL '30 days')
);

-- How actions reference notifs:
-- actions.notif_id = notifs.notif_id (the local Room id, unique per device).
-- actions.local_id is the client-generated idempotency key for the action itself.
-- A notif can have at most one action (notif_id is PK in actions).