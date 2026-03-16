const { WebSocketServer } = require('ws');
const jwt = require('jsonwebtoken');

const JWT_SECRET = process.env.JWT_SECRET || 'replace-this-in-production';

class SocketHub {
  constructor() {
    this.socketsByUserId = new Map();
    this.wss = new WebSocketServer({ noServer: true });

    this.wss.on('connection', (socket, request, userId) => {
      this.attachSocket(userId, socket);

      socket.send(
        JSON.stringify({
          type: 'CONNECTED',
          userId
        })
      );

      socket.on('close', () => {
        this.detachSocket(userId, socket);
      });
    });
  }

  handleUpgrade(request, socket, head) {
    try {
      const requestUrl = new URL(request.url, 'http://localhost');
      const token = requestUrl.searchParams.get('token');

      if (!token) {
        socket.write('HTTP/1.1 401 Unauthorized\r\n\r\n');
        socket.destroy();
        return;
      }

      const payload = jwt.verify(token, JWT_SECRET);
      const userId = Number(payload.userId);

      this.wss.handleUpgrade(request, socket, head, (ws) => {
        this.wss.emit('connection', ws, request, userId);
      });
    } catch (err) {
      socket.write('HTTP/1.1 401 Unauthorized\r\n\r\n');
      socket.destroy();
    }
  }

  attachSocket(userId, socket) {
    const set = this.socketsByUserId.get(userId) || new Set();
    set.add(socket);
    this.socketsByUserId.set(userId, set);
  }

  detachSocket(userId, socket) {
    const set = this.socketsByUserId.get(userId);
    if (!set) {
      return;
    }

    set.delete(socket);
    if (set.size === 0) {
      this.socketsByUserId.delete(userId);
    }
  }

  sendToUser(userId, message) {
    const payload = JSON.stringify(message);
    const set = this.socketsByUserId.get(userId);

    if (!set) {
      return;
    }

    for (const socket of set) {
      if (socket.readyState === socket.OPEN) {
        socket.send(payload);
      }
    }
  }
}

module.exports = SocketHub;
