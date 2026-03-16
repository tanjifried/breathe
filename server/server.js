const http = require('http');
const express = require('express');
require('dotenv').config();

require('./db/db');

const { authMiddleware, router: authRoutes } = require('./routes/auth');
const createStatusRouter = require('./routes/status');
const createSessionsRouter = require('./routes/sessions');
const SocketHub = require('./ws/hub');

const PORT = Number(process.env.PORT || 3000);

const app = express();
const server = http.createServer(app);
const hub = new SocketHub();

app.use(express.json());

app.get('/api/ping', (_req, res) => {
  res.json({ ok: true });
});

app.use('/api', authRoutes);
app.use('/api', createStatusRouter(authMiddleware, hub));
app.use('/api', createSessionsRouter(authMiddleware, hub));

app.use((_req, res) => {
  res.status(404).json({ error: 'Not found' });
});

server.on('upgrade', (request, socket, head) => {
  const isWebSocketRoute = request.url && request.url.startsWith('/ws');
  if (!isWebSocketRoute) {
    socket.destroy();
    return;
  }

  hub.handleUpgrade(request, socket, head);
});

server.listen(PORT, () => {
  console.log(`Breathe server listening on port ${PORT}`);
});
