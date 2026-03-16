# Breathe Server Plan (Tailored to This Server)

This plan follows `breathe-full-plan.md` and adapts it to the current Ubuntu Celeron server.

## Current Server Baseline

- CPU: Intel Celeron N4000 (2 cores)
- RAM: 3.6 GiB total, ~1.6 GiB available
- Disk: 98 GiB root, 64 GiB free
- Node.js: v22.22.1 (Node v20.20.0 also installed via nvm)
- npm: 10.9.4
- PM2: 6.0.14 (installed)
- Nginx: not installed yet
- Certbot: not installed yet

Assessment: hardware is sufficient for 2 users, WebSocket sync, SQLite, and voice-file hosting.

## What Was Installed

- Installed now: `pm2` globally with npm

Could not install system packages in this session because sudo password is required in this environment.

## Required Installs Still Pending (Run with sudo)

```bash
sudo apt update
sudo apt install -y nginx certbot python3-certbot-nginx ufw fail2ban sqlite3 build-essential python3 make g++
```

## Plan Sufficiency vs Current Device

The GitHub plan is strong on product architecture and feature roadmap, but needs server operations and security additions for production use.

Missing items to add:

1. Host hardening (firewall, fail2ban, SSH policy)
2. Secrets handling (`JWT_SECRET`, FCM credentials) outside repo
3. Backup and restore for SQLite and voice metadata
4. Voice-file privacy controls (avoid open public listing)
5. Nginx production defaults (redirect, limits, headers, timeouts)
6. Runtime pinning decision (Node 20 LTS vs current 22)

## Updated Server-First Roadmap

## S0 - Prereq and runtime pinning (before original Phase 0)

1. Keep current Node 22 for now, but run server under Node 20 if `better-sqlite3` build issues appear.
2. Install missing system packages listed above.
3. Create service layout:

```bash
sudo mkdir -p /var/breathe/{server,voices,data,backups,logs}
sudo chown -R $USER:$USER /var/breathe
chmod 750 /var/breathe
chmod 750 /var/breathe/voices
```

## S1 - Network and host hardening

```bash
sudo ufw allow OpenSSH
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw --force enable
sudo systemctl enable --now fail2ban
```

## S2 - Original Phase 0 implementation (server foundation)

Implement from `breathe-full-plan.md`:

- `server.js` with `GET /api/ping`
- SQLite schema and DB module
- auth + pair + join routes
- WS hub with JWT auth

Dependencies to install inside `/var/breathe/server`:

```bash
npm install express ws better-sqlite3 jsonwebtoken bcrypt node-cron multer dotenv cors helmet express-rate-limit
```

## S3 - Reverse proxy + TLS

1. Configure Nginx with:
   - HTTP to HTTPS redirect
   - `/api` proxy
   - `/ws` websocket upgrade
   - request body limits for voice uploads
   - security headers
2. Issue cert:

```bash
sudo certbot --nginx -d yourdomain.com
sudo certbot renew --dry-run
```

## S4 - Process management and startup

Use PM2 with ecosystem config and env file.

```bash
pm2 start server.js --name breathe
pm2 save
pm2 startup
```

Add log rotation:

```bash
pm2 install pm2-logrotate
```

## S5 - Data durability

1. Enable SQLite WAL mode and busy timeout in DB init.
2. Nightly backups of DB to `/var/breathe/backups`.
3. Weekly restore test to a temporary path.

## S6 - Security and privacy controls

1. JWT expiry + refresh flow.
2. Rate limit auth/pair/join/upload endpoints.
3. Strict CORS allowlist for extension/app origins.
4. Voice-file access via signed URLs or authenticated route (preferred) instead of broad static exposure.

## S7 - Continue original roadmap phases

After S0-S6, continue original phases in this order:

1. Phase 2 server-side status sync (fastest core value)
2. Phase 8 extension full sync
3. Phase 1 Android setup + pairing
4. Phase 3 sessions
5. Phase 4 voice studio
6. Phase 5/6/7/9 remaining features

## Verification Gates (required before each phase)

1. `curl https://yourdomain.com/api/ping` returns `{ ok: true }`
2. PM2 process is online after reboot
3. TLS cert valid and auto-renew dry-run passes
4. Backup file created and restore verified
5. Status sync and session events confirmed across both partner devices

## Immediate Next Commands (Server Owner)

Run these now on the server (with sudo access):

```bash
sudo apt update
sudo apt install -y nginx certbot python3-certbot-nginx ufw fail2ban sqlite3 build-essential python3 make g++
sudo ufw allow OpenSSH && sudo ufw allow 80/tcp && sudo ufw allow 443/tcp && sudo ufw --force enable
sudo systemctl enable --now nginx
sudo systemctl enable --now fail2ban
```

Then proceed with original Phase 0 in `/var/breathe/server`.
