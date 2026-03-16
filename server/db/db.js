const fs = require('fs');
const path = require('path');
const Database = require('better-sqlite3');

const dataDir = process.env.BREATHE_DATA_DIR || path.join(__dirname, '..', 'data');
const dbPath = process.env.BREATHE_DB_PATH || path.join(dataDir, 'breathe.db');

if (!fs.existsSync(dataDir)) {
  fs.mkdirSync(dataDir, { recursive: true });
}

const db = new Database(dbPath);
db.pragma('journal_mode = WAL');
db.pragma('foreign_keys = ON');
db.pragma('busy_timeout = 5000');

const schemaPath = path.join(__dirname, 'schema.sql');
const schemaSql = fs.readFileSync(schemaPath, 'utf-8');
db.exec(schemaSql);

const couplesColumns = db.prepare('PRAGMA table_info(couples)').all();
const hasCoolingUntil = couplesColumns.some((column) => column.name === 'cooling_until');

if (!hasCoolingUntil) {
  db.exec('ALTER TABLE couples ADD COLUMN cooling_until TEXT');
}

module.exports = db;
