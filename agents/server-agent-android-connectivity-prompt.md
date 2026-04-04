# Breathe Server Agent Prompt: Android Emulator Connectivity Fix

You are the `Backend Infrastructure Agent` for Breathe.

## Goal

Fix the self-hosted server stack so the Android emulator can authenticate and sync reliably against the local Breathe backend.

Current Android default server URL:

```text
http://10.0.2.2:3000/
```

The Android app now builds, installs, and launches successfully, but live sign-in and sync depend on the server being reachable from the emulator.

## Environment Context

- Project root contains:
  - `server/` Node.js + Express + SQLite + WebSocket backend
  - Android app under `android/`
- Deployment stack includes Docker and Nginx
- WebSocket endpoint currently uses JWT auth via query param:
  - `ws://<server>/ws?token=<jwt>`

## What Needs To Work

1. `POST /api/register`
2. `POST /api/login`
3. `POST /api/pair`
4. `POST /api/join`
5. `GET /api/status`
6. `POST /api/status`
7. `POST /api/sessions/start`
8. `POST /api/sessions/end`
9. `GET /api/insights/weekly`
10. `GET /api/checkins`
11. `POST /api/checkins`
12. WebSocket connection at `/ws`

## Suspected Failure Area

The likely issue is not Android code anymore. It is one or more of:

- the Node server binding only to localhost instead of `0.0.0.0`
- Docker port publishing not exposing the server correctly to the host
- Nginx not proxying `/api/*` and `/ws` correctly
- missing websocket upgrade headers in Nginx
- container-to-host networking mismatch
- server stack listening on a different port than Android expects

## Required Outcome

Make the backend reachable from the Android emulator through the host machine so this works from the app:

```text
http://10.0.2.2:3000/
```

If you choose a different final port or route shape, also document the exact Android `DEFAULT_SERVER_URL` value that should be used.

## Investigate And Fix

1. Verify how the Node server binds its host and port.
2. Verify Docker configuration:
   - `Dockerfile`
   - `docker-compose.yml` or compose equivalent
   - published ports
3. Verify Nginx configuration:
   - HTTP proxy for `/api/`
   - websocket proxy for `/ws`
   - upgrade headers
   - forwarded host/proto behavior
4. Confirm the server is reachable from the host at the documented URL.
5. Confirm websocket handshake works.
6. Keep the fix compatible with local self-hosted development.

## Validation Steps

Validate at minimum with commands equivalent to:

```bash
curl http://127.0.0.1:3000/api/ping
curl -X POST http://127.0.0.1:3000/api/register -H "Content-Type: application/json" -d '{"username":"androidtest","password":"secret123"}'
```

And verify websocket reachability for:

```text
ws://127.0.0.1:3000/ws?token=<jwt>
```

## Deliverable

Return:

1. the exact root cause
2. the files changed
3. the final URL Android should use
4. the commands used to validate the fix
5. any Nginx or Docker caveats for local Kubuntu development
