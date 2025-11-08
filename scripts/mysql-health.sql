-- MySQL Health Check Script
-- Purpose: Quick visibility into connection load, top tables, slow/hot queries, and locks
-- Usage:
--   mysql -h 127.0.0.1 -P 3307 -u <DB_USER> -p < scripts/mysql-health.sql

/* Server & Version */
SELECT @@hostname AS host, @@version AS version, @@version_comment AS distro, NOW() AS now_utc;

/* Uptime & Connections */
SELECT 
  VARIABLE_VALUE AS uptime_seconds
FROM performance_schema.global_status 
WHERE VARIABLE_NAME = 'Uptime';

SELECT 
  (SELECT VARIABLE_VALUE FROM performance_schema.global_status WHERE VARIABLE_NAME='Threads_connected') AS threads_connected,
  (SELECT VARIABLE_VALUE FROM performance_schema.global_status WHERE VARIABLE_NAME='Threads_running')   AS threads_running,
  (SELECT VARIABLE_VALUE FROM performance_schema.global_status WHERE VARIABLE_NAME='Connections')       AS connections_total,
  (SELECT VARIABLE_VALUE FROM performance_schema.global_variables WHERE VARIABLE_NAME='max_connections') AS max_connections;

/* Active sessions (top 50 by time) */
SELECT id, user, host, db, command, time, state, LEFT(info, 200) AS info
FROM information_schema.processlist
ORDER BY time DESC
LIMIT 50;

/* Database sizes (MB) */
SELECT 
  table_schema,
  ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) AS schema_mb
FROM information_schema.tables
GROUP BY table_schema
ORDER BY schema_mb DESC;

/* Top 20 largest tables (MB) */
SELECT 
  table_schema,
  table_name,
  ROUND((data_length + index_length) / 1024 / 1024, 2) AS size_mb,
  engine
FROM information_schema.tables
WHERE table_schema NOT IN ('mysql','information_schema','performance_schema','sys')
ORDER BY size_mb DESC
LIMIT 20;

/* InnoDB buffer pool hit ratio (approx) */
SELECT 
  1 - (
    (SELECT CAST(VARIABLE_VALUE AS DECIMAL(20,6)) FROM performance_schema.global_status WHERE VARIABLE_NAME='Innodb_buffer_pool_reads') /
    NULLIF((SELECT CAST(VARIABLE_VALUE AS DECIMAL(20,6)) FROM performance_schema.global_status WHERE VARIABLE_NAME='Innodb_buffer_pool_read_requests'),0)
  ) AS buffer_pool_hit_ratio;

/* Lock waits (requires sys schema) */
SELECT * FROM sys.innodb_lock_waits LIMIT 20;

/* Top queries by average time (requires performance_schema) */
SELECT 
  DIGEST_TEXT,
  COUNT_STAR,
  ROUND(AVG_TIMER_WAIT/1e12,3)  AS avg_seconds,
  ROUND(SUM_TIMER_WAIT/1e12,3)  AS total_seconds,
  SUM_ERRORS, SUM_WARNINGS
FROM performance_schema.events_statements_summary_by_digest
ORDER BY avg_seconds DESC
LIMIT 10;

/* Engine status (human-readable diagnostics) */
SHOW ENGINE INNODB STATUS;

