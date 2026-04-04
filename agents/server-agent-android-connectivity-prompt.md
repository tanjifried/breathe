# Breathe Server Agent Prompt: Local Reachability and Android Contract Reliability

You are the `Backend Infrastructure Agent` for Breathe.

## Goal

Keep the self-hosted Breathe backend reachable and contract-correct for the current Android app.

This is no longer just an emulator networking task. The server now needs to support:

- Android emulator local development
- physical Android phone development over `adb reverse`
- REST + WebSocket sync for the current feature set

## Current Repo Reality

- Backend lives in `server/`
- Stack is Node.js + Express + SQLite + WebSocket
- Main entrypoint is `server/server.js`
- The server now binds with:

```text
HOST=0.0.0.0
PORT=3000
```

- PM2 config is in:

```text
server/ecosystem.config.js
```

- Nginx deploy config is in:

```text
server/deploy/nginx-breathe.conf
```

- The current repo does not use Docker for this stack. Do not spend time on Docker unless the user explicitly adds it.

## Android Dev URLs

Current Android local development uses two valid server URLs depending on target:

1. Emulator:

```text
http://10.0.2.2:3000/
```

2. Physical phone over USB reverse:

```text
http://127.0.0.1:3000/
```

The Android app can route to either, and the dev runner configures `adb reverse tcp:3000 tcp:3000` for physical devices.

## WebSocket Contract

WebSocket endpoint:

```text
ws://<server>/ws?token=<jwt>
```

Current realtime message types used by Android include:

- `SESSION_STARTED`
- `SESSION_ENDED`
- `PEACE_SIGNAL`
- `QUICK_UPDATE`

## What Must Work

Authenticated REST routes currently used by the app:

1. `POST /api/register`
2. `POST /api/login`
3. `POST /api/pair`
4. `POST /api/join`
5. `GET /api/status`
6. `POST /api/status`
7. `POST /api/sessions/start`
8. `POST /api/sessions/end`
9. `POST /api/sessions/peace`
10. `GET /api/reentry-lock`
11. `GET /api/quick-updates`
12. `POST /api/quick-updates`
13. `GET /api/checkins`
14. `POST /api/checkins`
15. `GET /api/insights/weekly`
16. WebSocket connection at `/ws`

## Important Behavioral Expectations

- `/api/sessions/start` accepts `featureUsed` of `calm` or `timeout`
- `/api/sessions/peace` should notify the partner over WebSocket when available
- `/api/reentry-lock` reflects the current cooling window
- `/api/quick-updates` is couple-scoped and returns recent entries
- `POST /api/quick-updates` should persist the entry and fan it out over WebSocket to the partner
- Local self-hosted development must remain compatible with both emulator and physical phone workflows

## Investigate And Fix

When there is a connectivity or sync issue, investigate in this order:

1. Verify the Node server host and port binding in `server/server.js`
2. Verify PM2 runtime settings in `server/ecosystem.config.js`
3. Verify Nginx proxy behavior in `server/deploy/nginx-breathe.conf`
4. Verify `/api/*` reachability from the host
5. Verify WebSocket upgrade handling at `/ws`
6. Verify newer Android-used routes, especially:
   - `POST /api/sessions/peace`
   - `GET /api/reentry-lock`
   - `GET /api/quick-updates`
   - `POST /api/quick-updates`
7. Keep the final backend contract compatible with the current Android repo

## Suspected Failure Areas

The likely problem area is one or more of:

- wrong host/port binding
- PM2 env drift from the checked-in config
- Nginx `/api/` proxy mismatch
- missing Nginx websocket upgrade headers
- backend route drift from current Android expectations
- JWT/auth mismatch on REST or WebSocket
- local dev mismatch between emulator URL and physical-device URL

## Validation Steps

Validate with commands equivalent to:

```bash
curl http://127.0.0.1:3000/api/ping
curl -X POST http://127.0.0.1:3000/api/register -H "Content-Type: application/json" -d '{"username":"androidtest","password":"secret123"}'
curl -X POST http://127.0.0.1:3000/api/login -H "Content-Type: application/json" -d '{"username":"androidtest","password":"secret123"}'
```

Then validate pairing plus newer flows with authenticated requests:

```bash
POST /api/pair
POST /api/join
POST /api/quick-updates
GET /api/quick-updates?limit=5
POST /api/sessions/peace
GET /api/reentry-lock
```

And verify websocket reachability for:

```text
ws://127.0.0.1:3000/ws?token=<jwt>
```

## Required Outcome

Make the backend reachable and correct for the current Android app without breaking local self-hosted development.

If anything about the final port, route shape, or expected Android URL changes, document the exact Android value that should be used for:

1. emulator development
2. physical phone development over `adb reverse`

## Deliverable

Return:

1. the exact root cause
2. the files changed
3. the final Android URL for emulator development
4. the final Android URL for physical phone development
5. the commands used to validate the fix
6. any PM2 or Nginx caveats for local Kubuntu development
