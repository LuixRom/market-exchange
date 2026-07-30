ALTER TABLE usuario ADD COLUMN IF NOT EXISTS blocked BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS blocked_at TIMESTAMP;
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS blocked_reason VARCHAR(500);
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS suspended BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS suspended_at TIMESTAMP;
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS suspension_reason VARCHAR(500);

ALTER TABLE item ADD COLUMN IF NOT EXISTS rejection_reason VARCHAR(500);
ALTER TABLE item ADD COLUMN IF NOT EXISTS moderated_at TIMESTAMP;
ALTER TABLE item ADD COLUMN IF NOT EXISTS moderated_by_id BIGINT;

ALTER TABLE item
    ADD CONSTRAINT fk_item_moderated_by
    FOREIGN KEY (moderated_by_id) REFERENCES usuario(id);

CREATE TABLE IF NOT EXISTS item_moderation_history (
    id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL,
    moderator_id BIGINT NOT NULL,
    previous_status VARCHAR(40),
    new_status VARCHAR(40) NOT NULL,
    reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_item_moderation_item FOREIGN KEY (item_id) REFERENCES item(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_moderation_moderator FOREIGN KEY (moderator_id) REFERENCES usuario(id)
);

CREATE INDEX IF NOT EXISTS idx_item_moderation_item_created
    ON item_moderation_history (item_id, created_at DESC);

ALTER TABLE rating ADD COLUMN IF NOT EXISTS communication_score INTEGER;
ALTER TABLE rating ADD COLUMN IF NOT EXISTS punctuality_score INTEGER;
ALTER TABLE rating ADD COLUMN IF NOT EXISTS item_condition_score INTEGER;
ALTER TABLE rating ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

CREATE TABLE IF NOT EXISTS content_report (
    id BIGSERIAL PRIMARY KEY,
    target_type VARCHAR(40) NOT NULL,
    target_id BIGINT NOT NULL,
    reporter_id BIGINT NOT NULL,
    reason VARCHAR(120) NOT NULL,
    details VARCHAR(1000),
    status VARCHAR(40) NOT NULL DEFAULT 'PENDING',
    admin_notes VARCHAR(1000),
    reviewed_by_id BIGINT,
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_content_report_reporter FOREIGN KEY (reporter_id) REFERENCES usuario(id),
    CONSTRAINT fk_content_report_reviewed_by FOREIGN KEY (reviewed_by_id) REFERENCES usuario(id)
);

CREATE INDEX IF NOT EXISTS idx_content_report_status_created
    ON content_report (status, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_content_report_target
    ON content_report (target_type, target_id);
