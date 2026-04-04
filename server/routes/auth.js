const bcrypt = require('bcrypt');
const express = require('express');
const jwt = require('jsonwebtoken');

const db = require('../db/db');

const router = express.Router();

const JWT_SECRET = process.env.JWT_SECRET || 'replace-this-in-production';
const JWT_EXPIRES_IN = process.env.JWT_EXPIRES_IN || '7d';
const PAIRING_EXPIRY_MINUTES = 10;

const createToken = (userId) =>
  jwt.sign({ userId }, JWT_SECRET, {
    expiresIn: JWT_EXPIRES_IN
  });

const generatePairingCode = () =>
  String(Math.floor(Math.random() * 900000) + 100000);

const parseAuthHeader = (authHeader) => {
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return null;
  }
  return authHeader.slice('Bearer '.length);
};

const normalizeUserId = (value) => {
  const parsed = Number(value);
  if (!Number.isInteger(parsed) || parsed <= 0) {
    return null;
  }
  return parsed;
};

const authMiddleware = (req, res, next) => {
  const token = parseAuthHeader(req.header('Authorization'));

  if (!token) {
    return res.status(401).json({ error: 'Missing bearer token' });
  }

  try {
    const payload = jwt.verify(token, JWT_SECRET);
    const userId = normalizeUserId(payload.userId);
    if (!userId) {
      return res.status(401).json({ error: 'Invalid or expired token' });
    }
    req.userId = userId;
    return next();
  } catch (err) {
    return res.status(401).json({ error: 'Invalid or expired token' });
  }
};

router.post('/register', async (req, res) => {
  const { username, password } = req.body;

  if (!username || !password || password.length < 6) {
    return res.status(400).json({
      error: 'username and password are required. password must be at least 6 chars'
    });
  }

  const existingUser = db
    .prepare('SELECT id FROM users WHERE username = ?')
    .get(username.trim());

  if (existingUser) {
    return res.status(409).json({ error: 'Username already exists' });
  }

  try {
    const passwordHash = await bcrypt.hash(password, 12);

    const result = db
      .prepare('INSERT INTO users (username, password_hash) VALUES (?, ?)')
      .run(username.trim(), passwordHash);

    const token = createToken(result.lastInsertRowid);
    return res.status(201).json({
      token,
      user: {
        id: result.lastInsertRowid,
        username: username.trim()
      }
    });
  } catch (err) {
    return res.status(500).json({ error: 'Failed to register user' });
  }
});

router.post('/login', async (req, res) => {
  const { username, password } = req.body || {};

  if (!username || !password) {
    return res.status(400).json({
      error: 'username and password are required'
    });
  }

  const user = db
    .prepare('SELECT id, username, password_hash FROM users WHERE username = ?')
    .get(username.trim());

  if (!user) {
    return res.status(401).json({ error: 'Invalid credentials' });
  }

  try {
    const passwordMatches = await bcrypt.compare(password, user.password_hash);
    if (!passwordMatches) {
      return res.status(401).json({ error: 'Invalid credentials' });
    }

    const token = createToken(user.id);
    return res.json({
      token,
      user: {
        id: user.id,
        username: user.username
      }
    });
  } catch (err) {
    return res.status(500).json({ error: 'Failed to log in user' });
  }
});

router.post('/pair', authMiddleware, (req, res) => {
  const user = db
    .prepare('SELECT id, couple_id FROM users WHERE id = ?')
    .get(req.userId);

  if (!user) {
    return res.status(404).json({ error: 'User not found' });
  }

  if (user.couple_id) {
    return res.status(400).json({ error: 'User already paired' });
  }

  const expiry = new Date(Date.now() + PAIRING_EXPIRY_MINUTES * 60 * 1000).toISOString();
  let pairingCode = generatePairingCode();

  while (db.prepare('SELECT id FROM couples WHERE pairing_code = ?').get(pairingCode)) {
    pairingCode = generatePairingCode();
  }

  const result = db
    .prepare(
      `INSERT INTO couples (partner_a_id, pairing_code, pairing_expires_at)
       VALUES (?, ?, ?)`
    )
    .run(req.userId, pairingCode, expiry);

  db.prepare('UPDATE users SET couple_id = ? WHERE id = ?').run(result.lastInsertRowid, req.userId);

  return res.status(201).json({
    coupleId: result.lastInsertRowid,
    pairingCode,
    expiresAt: expiry
  });
});

router.post('/join', authMiddleware, (req, res) => {
  const { pairingCode } = req.body;

  if (!pairingCode || !/^\d{6}$/.test(pairingCode)) {
    return res.status(400).json({ error: 'pairingCode must be a 6-digit string' });
  }

  const user = db
    .prepare('SELECT id, couple_id FROM users WHERE id = ?')
    .get(req.userId);

  if (!user) {
    return res.status(404).json({ error: 'User not found' });
  }

  if (user.couple_id) {
    return res.status(400).json({ error: 'User already paired' });
  }

  const couple = db
    .prepare('SELECT * FROM couples WHERE pairing_code = ?')
    .get(pairingCode);

  if (!couple) {
    return res.status(404).json({ error: 'Pairing code not found' });
  }

  if (couple.partner_b_id) {
    return res.status(409).json({ error: 'Pairing code already used' });
  }

  if (new Date(couple.pairing_expires_at).getTime() < Date.now()) {
    return res.status(410).json({ error: 'Pairing code expired' });
  }

  const tx = db.transaction(() => {
    db.prepare('UPDATE couples SET partner_b_id = ? WHERE id = ?').run(req.userId, couple.id);
    db.prepare('UPDATE users SET couple_id = ? WHERE id = ?').run(couple.id, req.userId);
  });

  tx();

  return res.json({
    coupleId: couple.id,
    pairedWith: couple.partner_a_id
  });
});

module.exports = {
  authMiddleware,
  router
};
