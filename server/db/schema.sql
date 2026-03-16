CREATE TABLE IF NOT EXISTS users (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  username TEXT NOT NULL UNIQUE,
  password_hash TEXT NOT NULL,
  fcm_token TEXT,
  couple_id INTEGER,
  created_at TEXT NOT NULL DEFAULT (datetime('now')),
  FOREIGN KEY (couple_id) REFERENCES couples(id)
);

CREATE TABLE IF NOT EXISTS couples (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  partner_a_id INTEGER NOT NULL,
  partner_b_id INTEGER,
  safe_word TEXT,
  cooling_until TEXT,
  created_at TEXT NOT NULL DEFAULT (datetime('now')),
  pairing_code TEXT NOT NULL UNIQUE,
  pairing_expires_at TEXT NOT NULL,
  FOREIGN KEY (partner_a_id) REFERENCES users(id),
  FOREIGN KEY (partner_b_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS status (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER NOT NULL,
  color TEXT NOT NULL CHECK (color IN ('green', 'yellow', 'red')),
  updated_at TEXT NOT NULL DEFAULT (datetime('now')),
  FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS conflict_log (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  couple_id INTEGER NOT NULL,
  triggered_by_user_id INTEGER NOT NULL,
  feature_used TEXT NOT NULL,
  started_at TEXT NOT NULL,
  duration_seconds INTEGER,
  mood_before INTEGER,
  mood_after INTEGER,
  private_note TEXT,
  shared INTEGER NOT NULL DEFAULT 0,
  FOREIGN KEY (couple_id) REFERENCES couples(id),
  FOREIGN KEY (triggered_by_user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS checkins (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER NOT NULL,
  mood INTEGER NOT NULL CHECK (mood BETWEEN 1 AND 5),
  checked_at TEXT NOT NULL DEFAULT (datetime('now')),
  FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS voice_files (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER NOT NULL,
  session_type TEXT NOT NULL CHECK (session_type IN ('calm', 'timeout')),
  prompt_index INTEGER NOT NULL,
  file_path TEXT NOT NULL,
  duration_seconds INTEGER,
  uploaded_at TEXT NOT NULL DEFAULT (datetime('now')),
  FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_users_couple_id ON users(couple_id);
CREATE INDEX IF NOT EXISTS idx_couples_pairing_code ON couples(pairing_code);
CREATE INDEX IF NOT EXISTS idx_status_user_id ON status(user_id);
CREATE INDEX IF NOT EXISTS idx_conflict_log_couple_id ON conflict_log(couple_id);
CREATE INDEX IF NOT EXISTS idx_checkins_user_id ON checkins(user_id);
