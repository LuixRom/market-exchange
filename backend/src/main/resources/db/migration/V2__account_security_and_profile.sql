UPDATE usuario SET email = LOWER(TRIM(email));

CREATE UNIQUE INDEX IF NOT EXISTS uq_usuario_email_normalized
    ON usuario (LOWER(email));

ALTER TABLE usuario ADD COLUMN IF NOT EXISTS email_verified BOOLEAN;
UPDATE usuario SET email_verified = TRUE WHERE email_verified IS NULL;
ALTER TABLE usuario ALTER COLUMN email_verified SET DEFAULT FALSE;
ALTER TABLE usuario ALTER COLUMN email_verified SET NOT NULL;

ALTER TABLE usuario ADD COLUMN IF NOT EXISTS email_verified_at TIMESTAMP;
UPDATE usuario SET email_verified_at = NOW() WHERE email_verified = TRUE AND email_verified_at IS NULL;

ALTER TABLE usuario ADD COLUMN IF NOT EXISTS bio VARCHAR(255);
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(255);
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS location VARCHAR(100);

CREATE TABLE IF NOT EXISTS account_token (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(120) NOT NULL UNIQUE,
    type VARCHAR(40) NOT NULL,
    usuario_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_account_token_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_account_token_token
    ON account_token (token);

CREATE INDEX IF NOT EXISTS idx_account_token_user_type
    ON account_token (usuario_id, type);

