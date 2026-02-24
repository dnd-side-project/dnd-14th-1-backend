ALTER TABLE users ADD COLUMN name VARCHAR(255);

UPDATE users SET name = SPLIT_PART(email, '@', 1) WHERE email IS NOT NULL;
