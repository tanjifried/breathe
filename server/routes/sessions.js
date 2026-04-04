const express = require('express');

const db = require('../db/db');

const VALID_FEATURES = new Set(['calm', 'timeout']);
const TEN_MINUTES_MS = 10 * 60 * 1000;
const TWENTY_MINUTES_MS = 20 * 60 * 1000;

const normalizeUserId = (value) => {
  const parsed = Number(value);
  if (!Number.isInteger(parsed) || parsed <= 0) {
    return null;
  }
  return parsed;
};

const parseMood = (value) => {
  if (value === undefined || value === null || value === '') {
    return null;
  }

  const numeric = Number(value);
  if (!Number.isInteger(numeric) || numeric < 1 || numeric > 5) {
    return null;
  }

  return numeric;
};

const getCoupleContext = (userId) =>
  db
    .prepare(
      `SELECT u.id AS user_id,
              u.couple_id,
              c.partner_a_id,
              c.partner_b_id,
              c.cooling_until
       FROM users u
       LEFT JOIN couples c ON c.id = u.couple_id
       WHERE u.id = ?`
    )
    .get(userId);

const getPartnerId = (context, userId) => {
  const normalizedUserId = normalizeUserId(userId);
  if (!normalizedUserId || !context || !context.couple_id) {
    return null;
  }

  if (context.partner_a_id === normalizedUserId) {
    return context.partner_b_id || null;
  }

  if (context.partner_b_id === normalizedUserId) {
    return context.partner_a_id || null;
  }

  return null;
};

const isLocked = (coolingUntil) => {
  if (!coolingUntil) {
    return false;
  }

  const untilMs = new Date(coolingUntil).getTime();
  if (Number.isNaN(untilMs)) {
    return false;
  }

  return untilMs > Date.now();
};

const createSessionsRouter = (authMiddleware, hub) => {
  const router = express.Router();

  router.post('/sessions/start', authMiddleware, (req, res) => {
    const { featureUsed, moodBefore } = req.body || {};

    if (!VALID_FEATURES.has(featureUsed)) {
      return res.status(400).json({ error: 'featureUsed must be calm or timeout' });
    }

    const parsedMoodBefore = parseMood(moodBefore);
    if (moodBefore !== undefined && parsedMoodBefore === null) {
      return res.status(400).json({ error: 'moodBefore must be an integer between 1 and 5' });
    }

    const context = getCoupleContext(req.userId);
    if (!context) {
      return res.status(404).json({ error: 'User not found' });
    }

    if (!context.couple_id) {
      return res.status(400).json({ error: 'User must be paired' });
    }

    const startedAt = new Date().toISOString();
    const insert = db
      .prepare(
        `INSERT INTO conflict_log (
           couple_id,
           triggered_by_user_id,
           feature_used,
           started_at,
           mood_before
         ) VALUES (?, ?, ?, ?, ?)`
      )
      .run(context.couple_id, req.userId, featureUsed, startedAt, parsedMoodBefore);

    let coolingUntil = context.cooling_until || null;
    const partnerId = getPartnerId(context, req.userId);

    if (partnerId) {
      const partnerSession = db
        .prepare(
          `SELECT started_at
           FROM conflict_log
           WHERE couple_id = ? AND triggered_by_user_id = ?
           ORDER BY id DESC
           LIMIT 1`
        )
        .get(context.couple_id, partnerId);

      if (partnerSession && partnerSession.started_at) {
        const partnerStartedAtMs = new Date(partnerSession.started_at).getTime();
        const withinWindow =
          !Number.isNaN(partnerStartedAtMs) && Date.now() - partnerStartedAtMs <= TEN_MINUTES_MS;

        if (withinWindow) {
          coolingUntil = new Date(Date.now() + TWENTY_MINUTES_MS).toISOString();
          db.prepare('UPDATE couples SET cooling_until = ? WHERE id = ?').run(
            coolingUntil,
            context.couple_id
          );
        }
      }

      hub.sendToUser(partnerId, {
        type: 'SESSION_STARTED',
        sessionId: insert.lastInsertRowid,
        featureUsed,
        startedAt,
        userId: req.userId
      });
    }

    const locked = isLocked(coolingUntil);

    return res.status(201).json({
      sessionId: insert.lastInsertRowid,
      startedAt,
      locked,
      until: locked ? coolingUntil : null
    });
  });

  router.post('/sessions/end', authMiddleware, (req, res) => {
    const { sessionId, moodAfter, privateNote, shared } = req.body || {};
    const parsedSessionId = Number(sessionId);

    if (!Number.isInteger(parsedSessionId) || parsedSessionId <= 0) {
      return res.status(400).json({ error: 'sessionId must be a positive integer' });
    }

    const parsedMoodAfter = parseMood(moodAfter);
    if (moodAfter !== undefined && parsedMoodAfter === null) {
      return res.status(400).json({ error: 'moodAfter must be an integer between 1 and 5' });
    }

    if (privateNote !== undefined && privateNote !== null && typeof privateNote !== 'string') {
      return res.status(400).json({ error: 'privateNote must be a string' });
    }

    if (shared !== undefined && typeof shared !== 'boolean') {
      return res.status(400).json({ error: 'shared must be a boolean' });
    }

    const session = db
      .prepare(
        `SELECT id,
                couple_id,
                triggered_by_user_id,
                feature_used,
                started_at,
                duration_seconds,
                mood_before
         FROM conflict_log
         WHERE id = ?`
      )
      .get(parsedSessionId);

    if (!session) {
      return res.status(404).json({ error: 'Session not found' });
    }

    if (session.triggered_by_user_id !== req.userId) {
      return res.status(403).json({ error: 'Only the owner can end this session' });
    }

    if (session.duration_seconds !== null && session.duration_seconds !== undefined) {
      return res.status(400).json({ error: 'Session already ended' });
    }

    const now = new Date();
    const startedAtMs = new Date(session.started_at).getTime();
    const durationSeconds = Number.isNaN(startedAtMs)
      ? 0
      : Math.max(0, Math.floor((now.getTime() - startedAtMs) / 1000));

    const safePrivateNote =
      privateNote === undefined || privateNote === null ? null : privateNote.trim();
    const sharedValue = shared === true ? 1 : 0;

    db.prepare(
      `UPDATE conflict_log
       SET duration_seconds = ?,
           mood_after = ?,
           private_note = ?,
           shared = ?
       WHERE id = ?`
    ).run(durationSeconds, parsedMoodAfter, safePrivateNote, sharedValue, parsedSessionId);

    const summary = db
      .prepare(
        `SELECT id AS sessionId,
                couple_id AS coupleId,
                triggered_by_user_id AS triggeredByUserId,
                feature_used AS featureUsed,
                started_at AS startedAt,
                duration_seconds AS durationSeconds,
                mood_before AS moodBefore,
                mood_after AS moodAfter,
                private_note AS privateNote,
                shared
         FROM conflict_log
         WHERE id = ?`
      )
      .get(parsedSessionId);

    const context = getCoupleContext(req.userId);
    const partnerId = getPartnerId(context, req.userId);

    if (partnerId) {
      hub.sendToUser(partnerId, {
        type: 'SESSION_ENDED',
        sessionId: parsedSessionId,
        durationSeconds,
        endedAt: now.toISOString(),
        userId: req.userId
      });
    }

    return res.json({
      ...summary,
      shared: Boolean(summary.shared)
    });
  });

  router.get('/reentry-lock', authMiddleware, (req, res) => {
    const context = getCoupleContext(req.userId);
    if (!context) {
      return res.status(404).json({ error: 'User not found' });
    }

    if (!context.couple_id) {
      return res.json({ locked: false, until: null });
    }

    const locked = isLocked(context.cooling_until);

    return res.json({
      locked,
      until: locked ? context.cooling_until : null
    });
  });

  return router;
};

module.exports = createSessionsRouter;
