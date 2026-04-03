const express = require('express');

const db = require('../db/db');

const ONE_WEEK_MS = 7 * 24 * 60 * 60 * 1000;

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
  if (!context || !context.couple_id) {
    return null;
  }

  if (context.partner_a_id === userId) {
    return context.partner_b_id || null;
  }

  return context.partner_a_id || null;
};

const getMoodStats = (userId, sinceIso) =>
  db
    .prepare(
      `SELECT COUNT(*) AS totalCheckins,
              ROUND(AVG(mood), 2) AS averageMood
       FROM checkins
       WHERE user_id = ?
         AND datetime(checked_at) >= datetime(?)`
    )
    .get(userId, sinceIso);

const getSessionStats = (coupleId, sinceIso) =>
  db
    .prepare(
      `SELECT COUNT(*) AS sessionsStarted,
              SUM(CASE WHEN feature_used = 'timeout' THEN 1 ELSE 0 END) AS timeoutSessions,
              SUM(CASE WHEN feature_used = 'calm' THEN 1 ELSE 0 END) AS calmSessions,
              SUM(CASE WHEN shared = 1 THEN 1 ELSE 0 END) AS sharedReflections,
              ROUND(AVG(CASE
                WHEN mood_before IS NOT NULL AND mood_after IS NOT NULL
                THEN mood_after - mood_before
              END), 2) AS averageMoodShift
       FROM conflict_log
       WHERE couple_id = ?
         AND datetime(started_at) >= datetime(?)`
    )
    .get(coupleId, sinceIso);

const getTopFeature = (sessionStats) => {
  if (!sessionStats || !sessionStats.sessionsStarted) {
    return null;
  }

  if ((sessionStats.timeoutSessions || 0) > (sessionStats.calmSessions || 0)) {
    return 'timeout';
  }

  if ((sessionStats.calmSessions || 0) > 0) {
    return 'calm';
  }

  return null;
};

const getDataCompleteness = (ownStats, partnerStats) => {
  const ownCount = ownStats ? ownStats.totalCheckins || 0 : 0;
  const partnerCount = partnerStats ? partnerStats.totalCheckins || 0 : 0;
  const combined = ownCount + partnerCount;

  if (combined >= 10) {
    return 'high';
  }

  if (combined >= 4) {
    return 'moderate';
  }

  return 'low';
};

const toMoodTrend = (ownStats, partnerStats, sessionStats) => {
  const ownAverageMood = ownStats && ownStats.averageMood !== null ? Number(ownStats.averageMood) : null;
  const partnerAverageMood =
    partnerStats && partnerStats.averageMood !== null ? Number(partnerStats.averageMood) : null;
  const averageMoodShift =
    sessionStats && sessionStats.averageMoodShift !== null ? Number(sessionStats.averageMoodShift) : null;

  let direction = 'steady';

  if (averageMoodShift !== null) {
    if (averageMoodShift >= 0.5) {
      direction = 'improving';
    } else if (averageMoodShift <= -0.5) {
      direction = 'strained';
    }
  } else if (ownAverageMood !== null) {
    if (ownAverageMood >= 4) {
      direction = 'grounded';
    } else if (ownAverageMood <= 2.5) {
      direction = 'strained';
    }
  }

  return {
    direction,
    averageMood: ownAverageMood,
    partnerAverageMood,
    averageMoodShift
  };
};

const buildPatterns = ({ ownStats, partnerStats, sessionStats, activeCoolingLock }) => {
  const patterns = [];
  const ownCheckins = ownStats ? ownStats.totalCheckins || 0 : 0;
  const partnerCheckins = partnerStats ? partnerStats.totalCheckins || 0 : 0;
  const sessionsStarted = sessionStats ? sessionStats.sessionsStarted || 0 : 0;
  const timeoutSessions = sessionStats ? sessionStats.timeoutSessions || 0 : 0;
  const sharedReflections = sessionStats ? sessionStats.sharedReflections || 0 : 0;
  const averageMoodShift =
    sessionStats && sessionStats.averageMoodShift !== null ? Number(sessionStats.averageMoodShift) : null;

  if (sessionsStarted >= 3) {
    patterns.push('Conflict tools were activated several times this week.');
  }

  if (timeoutSessions >= 2 && timeoutSessions >= (sessionStats.calmSessions || 0)) {
    patterns.push('Timeout carried more of the load than calm support, which suggests escalation often reached the red zone.');
  }

  if (ownCheckins + partnerCheckins < 4) {
    patterns.push('Daily mood check-in coverage was light, so the weekly picture is incomplete.');
  }

  if (averageMoodShift !== null && averageMoodShift > 0) {
    patterns.push('Logged sessions ended with a better mood than they began on average.');
  }

  if (averageMoodShift !== null && averageMoodShift < 0) {
    patterns.push('Logged sessions ended with a lower mood than they began on average.');
  }

  if (sharedReflections > 0) {
    patterns.push('At least one session ended with a shared reflection instead of keeping everything private.');
  }

  if (activeCoolingLock) {
    patterns.push('A cooling period is still active right now.');
  }

  return patterns;
};

const buildRecommendations = ({ ownStats, partnerStats, sessionStats, activeCoolingLock }) => {
  const recommendations = [];
  const ownCheckins = ownStats ? ownStats.totalCheckins || 0 : 0;
  const partnerCheckins = partnerStats ? partnerStats.totalCheckins || 0 : 0;
  const timeoutSessions = sessionStats ? sessionStats.timeoutSessions || 0 : 0;
  const calmSessions = sessionStats ? sessionStats.calmSessions || 0 : 0;
  const averageMoodShift =
    sessionStats && sessionStats.averageMoodShift !== null ? Number(sessionStats.averageMoodShift) : null;

  if (ownCheckins < 3 || (partnerStats && partnerCheckins < 3)) {
    recommendations.push('Log one quick mood check-in each day to improve next week\'s insight quality.');
  }

  if (timeoutSessions > calmSessions) {
    recommendations.push('Add a yellow-zone intervention before conflict peaks, such as a guided talk or calm prompt.');
  }

  if (averageMoodShift !== null && averageMoodShift <= 0) {
    recommendations.push('Review the wording and pacing of re-entry so sessions end with more relief than they started with.');
  }

  if (activeCoolingLock) {
    recommendations.push('Keep re-entry explicit and slow until the active cooling window has fully expired.');
  }

  if (recommendations.length === 0) {
    recommendations.push('This week looks stable. Keep using low-pressure check-ins and connection rituals while calm.');
  }

  return recommendations;
};

const buildHeadline = ({ sessionStats, moodTrend, dataCompleteness, activeCoolingLock }) => {
  if (activeCoolingLock) {
    return 'A cooldown is still active. Prioritize a slow re-entry over problem solving.';
  }

  if (dataCompleteness === 'low') {
    return 'This week has limited data. More check-ins will make the next snapshot more reliable.';
  }

  if ((sessionStats.timeoutSessions || 0) > (sessionStats.calmSessions || 0)) {
    return 'Timeout was the dominant regulation tool this week. The next opportunity is earlier intervention.';
  }

  if (moodTrend.averageMoodShift !== null && moodTrend.averageMoodShift > 0) {
    return 'Your regulation flow appears to be helping moods recover after activation.';
  }

  if ((sessionStats.sessionsStarted || 0) === 0) {
    return 'No conflict sessions were logged this week. Keep investing in connection while calm.';
  }

  return 'This week looks steady overall, with enough data to keep tuning your rituals.';
};

const createInsightsRouter = (authMiddleware) => {
  const router = express.Router();

  router.get('/insights/weekly', authMiddleware, (req, res) => {
    const context = getCoupleContext(req.userId);

    if (!context) {
      return res.status(404).json({ error: 'User not found' });
    }

    const partnerId = getPartnerId(context, req.userId);
    const periodStart = new Date(Date.now() - ONE_WEEK_MS).toISOString();
    const periodEnd = new Date().toISOString();
    const ownStats = getMoodStats(req.userId, periodStart);
    const partnerStats = partnerId ? getMoodStats(partnerId, periodStart) : null;
    const sessionStats = context.couple_id
      ? getSessionStats(context.couple_id, periodStart)
      : {
          sessionsStarted: 0,
          timeoutSessions: 0,
          calmSessions: 0,
          sharedReflections: 0,
          averageMoodShift: null
        };

    const coolingUntilMs = context.cooling_until ? new Date(context.cooling_until).getTime() : null;
    const activeCoolingLock = Number.isFinite(coolingUntilMs) ? coolingUntilMs > Date.now() : false;
    const moodTrend = toMoodTrend(ownStats, partnerStats, sessionStats);
    const dataCompleteness = getDataCompleteness(ownStats, partnerStats);
    const payload = {
      periodStart,
      periodEnd,
      headline: buildHeadline({
        sessionStats,
        moodTrend,
        dataCompleteness,
        activeCoolingLock
      }),
      topFeature: getTopFeature(sessionStats),
      dataCompleteness,
      weeklySummary: {
        checkinsLogged: ownStats.totalCheckins || 0,
        partnerCheckinsLogged: partnerStats ? partnerStats.totalCheckins || 0 : 0,
        averageMood: ownStats.averageMood !== null ? Number(ownStats.averageMood) : null,
        partnerAverageMood: partnerStats && partnerStats.averageMood !== null ? Number(partnerStats.averageMood) : null,
        sessionsStarted: sessionStats.sessionsStarted || 0,
        timeoutSessions: sessionStats.timeoutSessions || 0,
        calmSessions: sessionStats.calmSessions || 0,
        sharedReflections: sessionStats.sharedReflections || 0,
        averageMoodShift: sessionStats.averageMoodShift !== null ? Number(sessionStats.averageMoodShift) : null,
        activeCoolingLock,
        coolingUntil: activeCoolingLock ? context.cooling_until : null
      },
      moodTrend,
      patterns: buildPatterns({
        ownStats,
        partnerStats,
        sessionStats,
        activeCoolingLock
      }),
      recommendations: buildRecommendations({
        ownStats,
        partnerStats,
        sessionStats,
        activeCoolingLock
      })
    };

    return res.json(payload);
  });

  return router;
};

module.exports = createInsightsRouter;
