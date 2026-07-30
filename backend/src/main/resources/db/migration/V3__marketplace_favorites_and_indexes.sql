CREATE TABLE IF NOT EXISTS favorite_item (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_favorite_item_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT fk_favorite_item_item FOREIGN KEY (item_id) REFERENCES item(id),
    CONSTRAINT uq_favorite_item_user_item UNIQUE (usuario_id, item_id)
);

CREATE INDEX IF NOT EXISTS idx_favorite_item_usuario_created
    ON favorite_item (usuario_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_item_status_created
    ON item (status, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_item_category_status
    ON item (category_id, status);

CREATE INDEX IF NOT EXISTS idx_item_user_status
    ON item (user_id, status);

CREATE INDEX IF NOT EXISTS idx_item_condition_status
    ON item (condition, status);
