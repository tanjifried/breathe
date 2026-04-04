const express = require('express');

const db = require('../db/db');

const VALID_COLORS = new Set(['green', 'yellow', 'red']);

const normalizeUserId = (value) => {
  const parsed = Number(value);
  if (!Number.isInteger(parsed) || parsed <= 0) {
    return null;
  }
  return parsed;
};

const getPartnerId = (userId) => {
  const normalizedUserId = normalizeUserId(userId);
  if (!normalizedUserId) {
    return null;
  }

  const row = db
    .prepare(
      `SELECT c.partner_a_id, c.partner_b_id
       FROM users u
       JOIN couples c ON c.id = u.couple_id
       WHERE u.id = ?`
    )
    .get(normalizedUserId);

  if (!row) {
    return null;
  }

  if (row.partner_a_id === normalizedUserId) {
    return row.partner_b_id || null;
  }

  if (row.partner_b_id === normalizedUserId) {
    return row.partner_a_id || null;
  }

  return null;
};

const createStatusRouter = (authMiddleware, hub) => {
  const router = express.Router();

  router.post('/status', authMiddleware, (req, res) => {
    const { color } = req.body;

    if (!VALID_COLORS.has(color)) {
      return res.status(400).json({ error: 'color must be one of: green, yellow, red' });
    }

    db.prepare(
      `INSERT INTO status (user_id, color, updated_at)
       VALUES (?, ?, datetime('now'))`
    ).run(req.userId, color);

    const partnerId = getPartnerId(req.userId);

    if (partnerId) {
      hub.sendToUser(partnerId, {
        type: 'STATUS_UPDATE',
        userId: req.userId,
        color,
        updatedAt: new Date().toISOString()
      });
    }

    return res.json({ ok: true, color });
  });

  router.get('/status', authMiddleware, (req, res) => {
    const own = db
      .prepare(
        `SELECT color, updated_at AS updatedAt
         FROM status
         WHERE user_id = ?
         ORDER BY updated_at DESC, id DESC
         LIMIT 1`
      )
      .get(req.userId);

    const partnerId = getPartnerId(req.userId);
    let partner = null;

    if (partnerId) {
      const latest = db
        .prepare(
          `SELECT color, updated_at AS updatedAt
           FROM status
           WHERE user_id = ?
           ORDER BY updated_at DESC, id DESC
           LIMIT 1`
        )
        .get(partnerId);

      partner = latest ? { userId: partnerId, ...latest } : { userId: partnerId, color: null };
    }

    return res.json({
      me: own || { color: null },
      partner
    });
  });

  return router;
};

module.exports = createStatusRouter;
