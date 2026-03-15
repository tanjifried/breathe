# Breathe — Full Platform Plan
### *calm lives between the words*

**Platforms:** Browser Extension (Brave/Chrome) · Android App · Self-Hosted Ubuntu Server

A couples emotion regulation platform built across three layers: a browser extension that lives inside Facebook Messenger, an Android app that both partners carry, and a self-hosted Node.js server running on your own Celeron Ubuntu machine. All data stays in your house. No Google. No Firebase data storage. Completely private.

---

## The core problem this solves

The browser extension works great — but it is invisible to your partner. Their traffic light color never reaches you. Their timeout never alerts you. Everything is local. The server becomes the sync layer that connects both extensions and both phones into one shared system, with your own voice guiding each other through the hard moments.

---

## Architecture overview

```
Partner A (Brave browser)          Partner B (Brave browser)
  └── extension                      └── extension
        │                                   │
        └──────────┐         ┌──────────────┘
                   │         │
                   ▼         ▼
              ┌─────────────────────┐
              │   Nginx (SSL/HTTPS) │  ← your domain + Let's Encrypt (free)
              │   yourdomain.com    │
              └────────┬────────────┘
                       │
              ┌────────▼────────────┐
              │   Node.js server    │  ← runs on your Celeron Ubuntu
              │   Express + ws      │
              │   PM2 (always on)   │
              └──┬──────┬──────┬───┘
                 │      │      │
         ┌───────▼┐  ┌──▼───┐ ┌▼──────────┐
         │SQLite/ │  │/voice│ │ Cron jobs │
         │Postgres│  │files │ │ check-ins │
         └────────┘  └──────┘ └─────┬─────┘
                                    │
                              ┌─────▼──────┐
                              │ FCM HTTP   │  ← push notifications only
                              │ (free API) │    Google sees no data
                              └────────────┘

Partner A (Android)            Partner B (Android)
  └── app ──────────────────────── app
        │   WebSocket to server      │
        └──────────────────────────┘
```

---

## Tech stack

### Server (your Celeron Ubuntu)

| Layer | Choice | Why |
|-------|--------|-----|
| Runtime | Node.js v20 LTS | Lightweight, fast, huge ecosystem |
| Web framework | Express.js | Simple REST API setup |
| WebSocket | ws library | Raw WebSocket, no overhead |
| Database | SQLite (start) → PostgreSQL (if needed) | SQLite needs zero setup, handles 2 users forever |
| File storage | Local filesystem `/var/breathe/voices/` | Your server, your disk |
| Process manager | PM2 | Keeps Node.js alive on reboot or crash |
| Reverse proxy | Nginx | Handles SSL, routes traffic |
| SSL certificate | Let's Encrypt via Certbot | Free, auto-renews |
| Push notifications | FCM HTTP API (v1) | Free, server calls Google's API, no data exposed |
| Scheduled tasks | node-cron | Daily check-ins, weekly insights |
| Auth | JWT (JSON Web Tokens) | Stateless, simple, no session storage needed |

### Android App

| Layer | Choice | Why |
|-------|--------|-----|
| Language | Kotlin | Modern Android standard |
| UI | Jetpack Compose | Declarative, fast |
| HTTP client | Retrofit + OkHttp | Clean API calls |
| WebSocket client | OkHttp WebSocket | Same library, built-in |
| Local database | Room (SQLite) | Offline-first conflict log |
| Auth | JWT stored in EncryptedSharedPreferences | Secure local token storage |
| Voice recording | Android MediaRecorder | Built-in, no libraries needed |
| Voice playback | Android MediaPlayer | Same |
| Push notifications | FCM (receive only) | Free, device token sent to your server |

### Browser Extension
Already built — needs additions to connect to your server via WebSocket and REST.

---

## How the couple connects (pairing flow)

1. Partner A opens the app → taps "Create room" → server generates a 6-digit code and stores a `couple` record in SQLite
2. Partner B enters the code → server links both user accounts to the same couple record
3. Both devices receive a shared `coupleId` and JWT token
4. The browser extension is paired by entering the same code in the extension's settings page (Phase 8)
5. Either partner can unpair at any time from Settings → this invalidates the couple record on the server

**Server database structure:**
```sql
-- users table
id, username, fcm_token, couple_id, created_at

-- couples table
id, partner_a_id, partner_b_id, safe_word, created_at, pairing_code, pairing_expires_at

-- status table
id, user_id, color (green/yellow/red), updated_at

-- conflict_log table
id, couple_id, triggered_by_user_id, feature_used, started_at,
duration_seconds, mood_before, mood_after, private_note, shared

-- checkins table
id, user_id, mood (1-5), checked_at

-- voice_files table
id, user_id, session_type (calm/timeout), prompt_index, file_path, duration_seconds, uploaded_at
```

---

## Features

### Carried over from extension (same behavior, mobile UI)
- ① Safe word / Calm — fullscreen overlay, breathing orb, countdown timer, voice guide
- ② Traffic light — green / yellow / red status, synced to partner in real time
- ③ Structured timeout — blurred screen, countdown, voice guide
- ⑤ Re-entry script — "I felt ___ when ___. I need ___ from you." — injected into chat

### Server-powered features

#### Real-time partner status (WebSocket)
Both the extension and app maintain a persistent WebSocket connection to your server. When you change your status, the server broadcasts instantly to your partner's connected devices — browser and phone simultaneously. Latency under 50ms on a local network, under 200ms over the internet.

#### Voice guide studio (your voice, stored on your server)
Record 6–10 voice prompts per session type (Calm and Timeout). Recordings upload to your server at `/var/breathe/voices/{coupleId}/{userId}/calm_01.aac` etc. During sessions, the app downloads and plays them at the correct timestamps using a `Handler` with `postDelayed`. Your partner hears your actual voice. Both partners record their own guides. Each can choose whose voice plays.

**Suggested prompt timing for Calm (20 min):**

| Timestamp | Suggested words |
|-----------|----------------|
| 0:00 | "Take a deep breath. You are safe." |
| 1:30 | "Let your shoulders drop. Keep breathing." |
| 5:00 | "You are doing well. Stay with it." |
| 10:00 | "Halfway there. You are handling this." |
| 18:00 | "Almost done. One more breath." |
| 20:00 | "Ready to talk? Return with kindness." |

Record whatever words feel natural. The slight imperfections in your real voice are exactly what make it work.

#### Conflict log (stored in your server's SQLite)
Every Calm or Timeout session auto-creates a log entry. Captures who triggered it, which feature, how long, mood before and after (optional), and a short private note. Both partners can view shared entries. Logs never leave your server.

#### Peace signal (server → FCM → partner's phone)
After a session, one tap sends "I'm calm now" to your server. The server calls the FCM HTTP API with your partner's device token. Google sends the push. Your partner's phone chimes softly. Google only sees a device token and a push payload — no names, no content, no couple data.

#### Daily check-in (server cron → FCM → both phones)
A `node-cron` job on your server fires every morning at your chosen time. It calls the FCM HTTP API to push a mood prompt to both phones. Tapping a mood emoji sends a single number (1–5) to your server. If both moods are low for 2+ consecutive days, the server pushes a soft suggestion to open the re-entry script.

#### Cooling period enforcer (server-side rule — cannot be bypassed)
If both partners trigger Timeout within 10 minutes of each other, the server locks the re-entry script for the full 20 minutes regardless of what the app says. This logic lives on the server — neither phone can skip it. When the lock expires, both partners receive a simultaneous push: "Your cooling period has ended."

#### Mood pattern alerts (server cron job)
Every night at midnight, a cron job checks the last 3 days of status logs. If both partners have been yellow or red on most days, it pushes a gentle message to both phones: "You've both had a hard few days. Be gentle with each other."

#### Weekly insights (server-generated, Sunday)
Every Sunday morning, a cron job queries the SQLite database and computes a weekly summary per couple: sessions per day, average mood before/after, which features were used most, trend direction. Pushed to both phones and available in the app's Insights screen. Entirely server-side — no client computation needed.

#### Gratitude note (after session)
When both partners have signalled "ready" after a conflict, the app prompts: "Want to send a kind word?" The text sends via your server to the partner's phone as a push notification. Optional, never required, never logged.

#### Extension ↔ App bridge (via server WebSocket)
The browser extension connects to the same WebSocket server as the Android apps. When you trigger Timeout on desktop, the server relays the event to your partner's phone immediately. When your partner goes red on their phone, your browser widget shows their color dot within milliseconds. All four devices — two extensions, two phones — are one connected system.

---

## Server setup (your Celeron Ubuntu)

### One-time server setup

```bash
# 1. Install Node.js, Nginx, Certbot
sudo apt update
sudo apt install -y nodejs npm nginx certbot python3-certbot-nginx

# 2. Install PM2 globally
sudo npm install -g pm2

# 3. Point your domain to your server's public IP (router port forward 80 + 443)
#    Then get a free SSL certificate
sudo certbot --nginx -d yourdomain.com

# 4. Create the app directory and voice storage
sudo mkdir -p /var/breathe/voices
sudo chown -R $USER /var/breathe

# 5. Clone/upload your server code to /var/breathe/server/
cd /var/breathe/server
npm install

# 6. Start with PM2 and save so it survives reboots
pm2 start server.js --name breathe
pm2 save
pm2 startup  # follow the command it outputs
```

### Nginx config (routes HTTPS and WebSocket)

```nginx
server {
    listen 443 ssl;
    server_name yourdomain.com;

    ssl_certificate /etc/letsencrypt/live/yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/yourdomain.com/privkey.pem;

    # REST API
    location /api/ {
        proxy_pass http://localhost:3000;
        proxy_set_header Host $host;
    }

    # WebSocket
    location /ws {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }

    # Voice files
    location /voices/ {
        alias /var/breathe/voices/;
        add_header Cache-Control "max-age=86400";
    }
}
```

### Server folder structure

```
/var/breathe/
├── server/
│   ├── server.js          ← main entry (Express + WebSocket)
│   ├── routes/
│   │   ├── auth.js        ← login, pairing, JWT
│   │   ├── status.js      ← get/set traffic light color
│   │   ├── sessions.js    ← start/end calm/timeout, conflict log
│   │   ├── voice.js       ← upload/download voice files
│   │   ├── checkin.js     ← daily mood
│   │   └── insights.js    ← weekly summary query
│   ├── ws/
│   │   └── hub.js         ← WebSocket connection manager
│   ├── cron/
│   │   ├── checkin.js     ← daily mood push (node-cron)
│   │   ├── insights.js    ← Sunday weekly summary
│   │   └── patterns.js    ← nightly mood pattern check
│   ├── db/
│   │   ├── schema.sql     ← SQLite table definitions
│   │   └── db.js          ← better-sqlite3 connection
│   ├── fcm.js             ← FCM HTTP v1 push helper
│   └── package.json
└── voices/                ← uploaded audio files live here
    └── {coupleId}/
        └── {userId}/
            ├── calm_00.aac
            ├── calm_01.aac
            └── timeout_00.aac
```

---

## Development roadmap

---

### Phase 0 — Server foundation
*Estimated time: 1 day*

Before building the app or updating the extension, get the server running.

1. SSH into your Ubuntu server and run the one-time setup commands above
2. Write `server.js` — Express app on port 3000, basic health check route `GET /api/ping → { ok: true }`
3. Write `db/schema.sql` and `db/db.js` — create the SQLite tables listed above using `better-sqlite3`
4. Write `auth.js` routes:
   - `POST /api/register` — creates a user, returns JWT
   - `POST /api/pair` — creates a couple with a 6-digit code, 10-minute expiry
   - `POST /api/join` — joins with a code, links both users
5. Write `ws/hub.js` — WebSocket server that authenticates connections via JWT query param, keeps a `Map<userId, socket>`
6. Start with PM2, confirm `https://yourdomain.com/api/ping` returns `{ ok: true }`

**npm packages needed:**
```bash
npm install express ws better-sqlite3 jsonwebtoken bcrypt node-cron multer
```

---

### Phase 1 — Android app: setup and pairing
*Estimated time: 1–2 days*

1. Create Android Studio project — Kotlin + Jetpack Compose + Material3
2. Add Retrofit + OkHttp dependencies to `build.gradle`
3. Store server base URL in `BuildConfig` (e.g. `https://yourdomain.com`)
4. Build the registration and pairing flow:
   - Register screen → `POST /api/register` → save JWT in `EncryptedSharedPreferences`
   - "Create room" → `POST /api/pair` → show 6-digit code
   - "Join room" → enter code → `POST /api/join` → both linked
5. Build a minimal home screen confirming "Connected to partner"
6. Register device FCM token → `POST /api/fcm-token` → server stores it for push delivery

---

### Phase 2 — Real-time status sync (WebSocket)
*Estimated time: 1–2 days*

**Server side (`routes/status.js` + `ws/hub.js`):**
1. `POST /api/status` — saves color to SQLite, then broadcasts to partner's WebSocket connection via the hub
2. `GET /api/status` — returns both partners' current colors

**Android side:**
1. Open a persistent OkHttp WebSocket connection to `wss://yourdomain.com/ws?token={jwt}` on app start
2. Parse incoming messages — `{ type: "STATUS_UPDATE", userId, color }`
3. Update the partner's status card in real time via a `StateFlow`
4. Reconnect automatically on disconnect using exponential backoff
5. If partner goes red, trigger a short vibration pattern

**Extension side (`status.js` update):**
1. Open a WebSocket to your server alongside the existing `chrome.storage.local` logic
2. On status change, send `{ type: "STATUS_UPDATE", color }` to the server
3. Listen for incoming `STATUS_UPDATE` messages and update the partner color dot in the widget

---

### Phase 3 — Calm and Timeout sessions
*Estimated time: 2 days*

**Server side (`routes/sessions.js`):**
1. `POST /api/sessions/start` — logs session start to SQLite, broadcasts event to partner via WebSocket
2. `POST /api/sessions/end` — logs duration and mood, marks session complete
3. Cooling period enforcer: if both partners start a session within 10 minutes, set a `cooling_until` timestamp on the couple record. Return a `{ locked: true, until }` flag on re-entry script requests

**Android side:**
1. Build Calm screen — breathing orb animation (scale + opacity CSS equivalent in Compose), countdown timer
2. Build Timeout screen — dark overlay, countdown timer
3. On session start: call `POST /api/sessions/start`, also write to local Room database for offline fallback
4. On session end: show mood prompt (1–5 stars), call `POST /api/sessions/end`
5. Peace signal button: `POST /api/peace` → server calls FCM HTTP API to push a chime to partner

---

### Phase 4 — Voice guide studio
*Estimated time: 2–3 days*

This is the most personal feature. Your voice guides your partner through their hardest moments.

**Server side (`routes/voice.js`):**
1. `POST /api/voice/upload` — accepts multipart AAC file, saves to `/var/breathe/voices/{coupleId}/{userId}/calm_{index}.aac`
2. `GET /api/voice/list` — returns list of uploaded prompt files for the couple
3. Voice files are served directly by Nginx at `https://yourdomain.com/voices/...` with cache headers

**Android side:**
1. Build the Voice Studio screen — list of 6 timed prompt slots per session type
2. For each slot: record button → Android `MediaRecorder` → AAC file → upload to server
3. Preview button: plays back the uploaded file via `MediaPlayer`
4. Download partner's voice files on first load, cache in app's internal storage
5. During Calm/Timeout sessions, use `Handler.postDelayed` to play the correct file at each timestamp
6. Settings preference: "Guide voice — Mine / Partner's / None"

---

### Phase 5 — Conflict log
*Estimated time: 1–2 days*

Sessions are already being logged to the server in Phase 3. This phase builds the UI and the mood + notes layer.

**Android side:**
1. Build the Conflict Log screen — `LazyColumn` of entries, each expandable
2. Each entry shows: date, time, who triggered, which feature, duration, mood before/after, optional note
3. After each session ends, show a brief bottom sheet: "How do you feel now?" (5 stars + optional note field)
4. "Share with partner" toggle on each entry — calls `PATCH /api/sessions/{id}/share`

**Server side:**
1. `GET /api/sessions` — returns couple's shared conflict log sorted by date
2. `PATCH /api/sessions/{id}/share` — marks entry as shared, broadcasts to partner via WebSocket

---

### Phase 6 — Daily check-in + peace signal
*Estimated time: 1 day*

**Server side (`cron/checkin.js`):**
1. `node-cron` job fires at configurable time (default 8:00 AM, stored per couple in SQLite)
2. Job queries all couples' FCM tokens, calls FCM HTTP v1 API with a mood prompt notification
3. `POST /api/checkin` — receives mood (1–5) from phone, saves to SQLite
4. After both partners check in, server compares moods — if both ≤ 2 for 2 consecutive days, pushes a suggestion to open the re-entry script

**Android side:**
1. Handle the incoming FCM notification — show inline action buttons (😔 😐 🙂 😊 😄)
2. Tapping a mood sends `POST /api/checkin` in the background
3. Home screen shows today's check-in status for both partners once both have responded

---

### Phase 7 — Server-side intelligent features
*Estimated time: 1–2 days*

These run entirely on your server with no app code changes.

**`cron/patterns.js` — nightly mood pattern check:**
```js
// Runs at midnight every day
// If couple has been yellow/red for 3+ of the last 5 days:
// → Push: "You've both had a hard few days. Be gentle with each other."
```

**`cron/insights.js` — Sunday weekly summary:**
```js
// Runs every Sunday at 9:00 AM
// Queries conflict_log for the past 7 days
// Computes: sessions per day, avg mood before/after, most used feature
// Generates a 2–3 sentence observation string
// Saves to weekly_insights table, pushes notification to both phones
```

**Cooling period enforcer** (already in Phase 3 server logic):
- No app changes needed — the server returns `{ locked: true }` and the app respects it

---

### Phase 8 — Extension update: full server sync
*Estimated time: 1–2 days*

Update the existing Breathe extension to connect to your server.

1. Add a Settings page (`options.html`) where you enter:
   - Server URL (e.g. `https://yourdomain.com`)
   - Your JWT token (generated by the server after login)
2. Update `status.js` — after `chrome.storage.local.set()`, also POST to `/api/status`
3. Update `content.js` — open a WebSocket connection, listen for `STATUS_UPDATE` from partner, show their color dot
4. Update `calm.js` and `timeout.js` — call `/api/sessions/start` and `/api/sessions/end`
5. Add voice guide playback to the extension's Calm and Timeout overlays — fetch voice files from `https://yourdomain.com/voices/...` and play via Web Audio API
6. The partner's status dot in the widget now shows their real color, live

**Extension sync code example (`status.js` addition):**
```js
const SERVER = await getServerUrl(); // from chrome.storage.local
const TOKEN = await getToken();

async function syncStatus(color) {
  await fetch(`${SERVER}/api/status`, {
    method: 'POST',
    headers: { 'Authorization': `Bearer ${TOKEN}`, 'Content-Type': 'application/json' },
    body: JSON.stringify({ color })
  });
}

// WebSocket listener for partner's color
const ws = new WebSocket(`${SERVER.replace('https','wss')}/ws?token=${TOKEN}`);
ws.onmessage = (event) => {
  const msg = JSON.parse(event.data);
  if (msg.type === 'STATUS_UPDATE') {
    updatePartnerDot(msg.color); // show on widget
  }
};
```

---

### Phase 9 — Polish and release
*Estimated time: 2–3 days*

1. Build onboarding flow in the Android app: welcome → register → pair → grant permissions
2. Add settings screen: server URL, timer duration, voice preference, check-in time, safe word, unpair
3. Add dark mode (Compose Material3 `dynamicColorScheme` handles most of it)
4. Add offline fallback: if server is unreachable, the app still works locally — sessions log to Room, sync when connection returns
5. Test on Android API 26+ (Android 8.0 and above)
6. Distribute to your partner: generate a signed APK → share via Google Drive or direct install
7. No Play Store needed — just enable "Install from unknown sources" on her phone

---

## Total estimated build time

| Phase | Task | Time |
|-------|------|------|
| 0 | Server foundation | 1 day |
| 1 | Android setup + pairing | 1–2 days |
| 2 | Real-time status (WebSocket) | 1–2 days |
| 3 | Calm + Timeout sessions | 2 days |
| 4 | Voice guide studio | 2–3 days |
| 5 | Conflict log | 1–2 days |
| 6 | Daily check-in + peace signal | 1 day |
| 7 | Server-side intelligent features | 1–2 days |
| 8 | Extension full sync | 1–2 days |
| 9 | Polish + release | 2–3 days |
| **Total** | | **13–20 days** |

Building with Claude generating each module cuts this by 40–50%. Start with Phase 0 — the server — because every other phase depends on it.

---

## What to build first (recommended order)

1. **Phase 0** — get the server running and `ping` returning. Paste your server URL here and I'll write `server.js` for you.
2. **Phase 2 server side** — WebSocket hub and status routes. This fixes the biggest gap in the extension immediately.
3. **Phase 8 extension update** — connect the existing extension to your server. Both of you now see each other's color dot in real time. This alone is a huge upgrade.
4. **Phase 1 + 2 Android** — basic app with real-time status. Core value delivered.
5. **Phase 4** — voice guide. The most personal feature.
6. Everything else in order.

---

## Why your server beats Firebase for this

| Concern | Firebase | Your Celeron |
|---------|----------|--------------|
| Your fight logs | On Google's servers | On your machine |
| Voice recordings | Charged after 5GB | Unlimited, free |
| Real-time sync | Firestore (polling-ish) | WebSocket (true real-time) |
| Custom logic (cooling enforcer, pattern alerts) | Cloud Functions (paid) | Just Node.js code |
| Monthly cost | Free tier → then billed | Electricity only |
| Data ownership | Google's ToS | Yours, completely |

For two users, your Celeron will never be stressed. A WebSocket server for 2 concurrent connections uses under 20MB RAM and near-zero CPU. You could run this on a router.

---

## The voice guide — a note

During emotional flooding, a familiar voice activates the parasympathetic nervous system faster than any other stimulus. Hearing your partner's voice — their actual voice, with its natural warmth and imperfections — during a 20-minute timeout is not a gimmick. It is physiologically regulating in a way that a meditation app voice simply is not.

Record your guides on a good day, when you are both relaxed and happy. Keep the tone soft and unhurried. Do not over-script it. A slight stumble or a quiet laugh is fine — it makes the voice more real, not less effective.

---

## The golden rule

> Set up the server, record the voices, and pair your devices **when you are both calm and happy** — not during or after conflict. The system is most powerful when both partners have already decided, together, to trust it.
