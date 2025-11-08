-- MySQL Data Introspection Script (current schema)
-- Shows tables, approx sizes, and optional exact row counts
-- Usage examples:
--   1) Connect with schema: mysqlsh --sql root@127.0.0.1:3307/zapp_demo_db -p -f scripts/mysql-introspect.sql
--   2) Or: mysqlsh --sql -h 127.0.0.1 -P 3307 -u <DB_USER> -p
--      then run: \source scripts/mysql-introspect.sql

/* Current database */
SELECT DATABASE() AS current_database, NOW() AS now_utc;

/* Tables overview (approx rows) */
SELECT 
  table_name,
  engine,
  table_rows AS approx_rows,
  ROUND((data_length + index_length)/1024/1024, 2) AS size_mb,
  create_time,
  update_time
FROM information_schema.tables
WHERE table_schema = DATABASE()
ORDER BY size_mb DESC, table_name;

/* Optional: exact row counts per table (may be slow on large tables) */
DELIMITER //
DROP PROCEDURE IF EXISTS sp_row_counts //
CREATE PROCEDURE sp_row_counts(IN db_name VARCHAR(64))
BEGIN
  DECLARE done INT DEFAULT FALSE;
  DECLARE t VARCHAR(256);
  DECLARE cur CURSOR FOR 
    SELECT table_name FROM information_schema.tables WHERE table_schema = db_name AND table_type='BASE TABLE';
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

  CREATE TEMPORARY TABLE IF NOT EXISTS _row_counts (
    table_name VARCHAR(256),
    cnt BIGINT
  );
  TRUNCATE TABLE _row_counts;

  OPEN cur;
  read_loop: LOOP
    FETCH cur INTO t;
    IF done THEN LEAVE read_loop; END IF;
    SET @sql = CONCAT('INSERT INTO _row_counts SELECT ''', t, ''', COUNT(*) FROM `', db_name, '`.`', t, '`');
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END LOOP;
  CLOSE cur;

  SELECT * FROM _row_counts ORDER BY cnt DESC, table_name;
  DROP TEMPORARY TABLE _row_counts;
END //
DELIMITER ;

CALL sp_row_counts(DATABASE());

/* Example: peek first rows from a specific table (edit table name) */
-- SELECT * FROM `users` LIMIT 50;

