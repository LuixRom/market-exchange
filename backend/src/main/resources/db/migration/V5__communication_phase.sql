ALTER TABLE trade_proposal ADD COLUMN IF NOT EXISTS initial_message VARCHAR(500);
ALTER TABLE trade_proposal ADD COLUMN IF NOT EXISTS cancelled_at TIMESTAMP;

CREATE TABLE IF NOT EXISTS notification (
    id BIGSERIAL PRIMARY KEY,
    recipient_id BIGINT NOT NULL,
    type VARCHAR(80) NOT NULL,
    title VARCHAR(140) NOT NULL,
    message VARCHAR(500) NOT NULL,
    trade_proposal_id BIGINT,
    item_id BIGINT,
    read_flag BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    read_at TIMESTAMP,
    CONSTRAINT fk_notification_recipient FOREIGN KEY (recipient_id) REFERENCES usuario(id)
);

CREATE INDEX IF NOT EXISTS idx_notification_recipient_created
    ON notification (recipient_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_notification_recipient_unread
    ON notification (recipient_id, read_flag);

CREATE TABLE IF NOT EXISTS chat_message (
    id BIGSERIAL PRIMARY KEY,
    trade_proposal_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    read_at TIMESTAMP,
    CONSTRAINT fk_chat_message_trade_proposal FOREIGN KEY (trade_proposal_id) REFERENCES trade_proposal(id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_message_sender FOREIGN KEY (sender_id) REFERENCES usuario(id)
);

CREATE INDEX IF NOT EXISTS idx_chat_message_trade_created
    ON chat_message (trade_proposal_id, created_at ASC);
