ALTER TABLE app_user
    ADD COLUMN IF NOT EXISTS password_reset_token  VARCHAR(180),
    ADD COLUMN IF NOT EXISTS password_reset_expiry TIMESTAMP;

CREATE UNIQUE INDEX IF NOT EXISTS ux_user_reset_token
    ON app_user(password_reset_token)
    WHERE password_reset_token IS NOT NULL;
