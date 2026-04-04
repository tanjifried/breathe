const express = require('express');

const db = require('../db/db');

const MAX_LIMIT = 30;
const MAX_MESSAGE_LENGTH = 80;
const MAX_NOTE_LENGTH = 160;

const parseLimit = (value) => {
  if (value === undefined) {
    return 12;
  }

  const numeric = Number(value);
  if (!Number.isInteger(numeric) || numeric < 1 || numeric > MAX_LIMIT) {
    return null;
  }

  return numeric;
};

const normalizeText = (value) => (typeof value === 'string' ? value.trim() : '');

const getCoupleContext = (userId) =>
  db
    .prepare(
      `SELECT u.id AS user_id,
              u.couple_id,
              c.partner_a_id,
              c.partner_b_id
       FROM users u
       LEFT JOIN couples c ON c.id = u.couple_id
       WHERE u.id = ?`
    )
    .get(userId);

const getPartnerId = (context, userId) => {
  if (!context || !context.couple_id) {
    return null;
  }

  if (context.partner_a_id === userId) {
    return context.partner_b_id || null;
  }

  return context.partner_a_id || null;
};

const selectEntries = (coupleId, limit) =>
  db
    .prepare(
      `SELECT id AS updateId,
              sender_user_id AS senderUserId,
              preset_key AS presetKey,
              message,
              note,
              created_at AS createdAt
       FROM quick_updates
       WHERE couple_id = ?
       ORDER BY datetime(created_at) DESC, id DESC
       LIMIT ?`
    )
    .all(coupleId, limit);

const createQuickUpdatesRouter = (authMiddleware, hub) => {
  const router = express.Router();

  router.get('/quick-updates', authMiddleware, (req, res) => {
    const context = getCoupleContext(req.userId);

    if (!context) {
      return res.status(404).json({ error: 'User not found' });
    }

    if (!context.couple_id) {
      return res.json({ entries: [], total: 0 });
    }

    const limit = parseLimit(req.query.limit);
    if (limit === null) {
      return res.status(400).json({ error: `limit must be an integer between 1 and ${MAX_LIMIT}` });
    }

    const entries = selectEntries(context.couple_id, limit);
    return res.json({ entries, total: entries.length });
  });

  router.post('/quick-updates', authMiddleware, (req, res) => {
    const context = getCoupleContext(req.userId);

    if (!context) {
      return res.status(404).json({ error: 'User not found' });
    }

    if (!context.couple_id) {
      return res.status(400).json({ error: 'User must be paired' });
    }

    const presetKey = normalizeText(req.body?.presetKey);
    const message = normalizeText(req.body?.message);
    const note = normalizeText(req.body?.note);

    if (!presetKey) {
      return res.status(400).json({ error: 'presetKey is required' });
    }

    if (!message || message.length > MAX_MESSAGE_LENGTH) {
      return res.status(400).json({ error: `message must be between 1 and ${MAX_MESSAGE_LENGTH} characters` });
    }

    if (note.length > MAX_NOTE_LENGTH) {
      return res.status(400).json({ error: `note must be ${MAX_NOTE_LENGTH} characters or fewer` });
    }

    const createdAt = new Date().toISOString();
    const insert = db
      .prepare(
        `INSERT INTO quick_updates (couple_id, sender_user_id, preset_key, message, note, created_at)
         VALUES (?, ?, ?, ?, ?, ?)`
      )
      .run(context.couple_id, req.userId, presetKey, message, note || null, createdAt);

    const payload = db
      .prepare(
        `SELECT id AS updateId,
                sender_user_id AS senderUserId,
                preset_key AS presetKey,
                message,
                note,
                created_at AS createdAt
         FROM quick_updates
         WHERE id = ?`
      )
      .get(insert.lastInsertRowid);

    const partnerId = getPartnerId(context, req.userId);
    if (partnerId) {
      hub.sendToUser(partnerId, {
        type: 'QUICK_UPDATE',
        ...payload
      });
    }

    return res.status(201).json(payload);
  });

  return router;
};

module.exports = createQuickUpdatesRouter;
