-- Preview users table: names, emails, hashed passwords
-- Usage:
--   mysqlsh --sql root@127.0.0.1:3307/zapp_demo_db -p -f scripts/users-preview.sql

SELECT COUNT(*) AS users_total FROM users;

SELECT 
  id,
  username,
  email,
  password,
  roles,
  enabled
FROM users
ORDER BY id DESC
LIMIT 100;

