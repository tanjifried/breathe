#!/usr/bin/env bash
set -euo pipefail

TS_DOMAIN="miyamoto.tail6fc25e.ts.net"
TS_IP="100.64.245.59"

echo "[1/6] Ensure tailscale cert can be generated without root next time"
sudo tailscale set --operator="$USER" || true

echo "[2/6] Refresh tailscale TLS cert"
sudo tailscale cert \
  --cert-file "/home/$USER/${TS_DOMAIN}.crt" \
  --key-file "/home/$USER/${TS_DOMAIN}.key" \
  --min-validity 720h \
  "$TS_DOMAIN"

echo "[3/6] Install nginx site config"
sudo cp \
  /home/$USER/Documents/Projects/breathe/server/deploy/nginx-breathe.conf \
  /etc/nginx/sites-available/breathe
sudo ln -sf /etc/nginx/sites-available/breathe /etc/nginx/sites-enabled/breathe
sudo rm -f /etc/nginx/sites-enabled/default

echo "[4/6] Validate and reload nginx"
sudo nginx -t
sudo systemctl reload nginx

echo "[5/6] Open tailnet-only web ports on host firewall"
sudo ufw allow in on tailscale0 to any port 80 proto tcp
sudo ufw allow in on tailscale0 to any port 443 proto tcp

echo "[6/6] Ensure PM2 app survives reboot"
pm2 start /home/$USER/Documents/Projects/breathe/server/ecosystem.config.js --env production || true
pm2 save
sudo env PATH="$PATH" /home/$USER/.nvm/versions/node/v22.22.1/bin/pm2 startup systemd -u "$USER" --hp "/home/$USER"

echo
echo "Done. Test from a tailnet client: https://${TS_DOMAIN}/api/ping"
echo "Expected: {\"ok\":true}"
