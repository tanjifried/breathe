const express = require('express');

const db = require('../db/db');

const parseMood = (value) => {
  const numeric = Number(value);

  if (!Number.isInteger(numeric) || numeric < 1 || numeric > 5) {
    return null;
  }

  return numeric;
};

const parseLimit = (value) => {
  if (value === undefined) {
    return 14;
  }

  const numeric = Number(value);
  if (!Number.isInteger(numeric) || numeric < 1 || numeric > 60) {
    return null;
  }

  return numeric;
};

const createCheckinsRouter = (authMiddleware) => {
  const router = express.Router();

  router.post('/checkins', authMiddleware, (req, res) => {
    const { mood } = req.body || {};
    const parsedMood = parseMood(mood);

    if (parsedMood === null) {
      return res.status(400).json({ error: 'mood must be an integer between 1 and 5' });
    }

    const insert = db.prepare('INSERT INTO checkins (user_id, mood) VALUES (?, ?)').run(req.userId, parsedMood);
    const created = db
      .prepare(
        `SELECT id AS checkinId,
                mood,
                checked_at AS checkedAt
         FROM checkins
         WHERE id = ?`
      )
      .get(insert.lastInsertRowid);

    return res.status(201).json(created);
  });

  router.get('/checkins', authMiddleware, (req, res) => {
    const limit = parseLimit(req.query.limit);

    if (limit === null) {
      return res.status(400).json({ error: 'limit must be an integer between 1 and 60' });
    }

    const entries = db
      .prepare(
        `SELECT id AS checkinId,
                mood,
                checked_at AS checkedAt
         FROM checkins
         WHERE user_id = ?
         ORDER BY datetime(checked_at) DESC, id DESC
         LIMIT ?`
      )
      .all(req.userId, limit);

    return res.json({
      entries,
      total: entries.length
    });
  });

  return router;
};

module.exports = createCheckinsRouter;
