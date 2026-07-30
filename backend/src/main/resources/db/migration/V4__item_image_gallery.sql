CREATE TABLE IF NOT EXISTS item_image (
    id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL,
    storage_key VARCHAR(255) NOT NULL,
    storage_provider VARCHAR(20) NOT NULL,
    primary_image BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_item_image_item FOREIGN KEY (item_id) REFERENCES item(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_item_image_item
    ON item_image (item_id);

CREATE INDEX IF NOT EXISTS idx_item_image_storage
    ON item_image (storage_key);

INSERT INTO item_image (item_id, storage_key, storage_provider, primary_image, sort_order, created_at)
SELECT id, image_key, image_provider, TRUE, 0, COALESCE(created_at, NOW())
FROM item
WHERE image_key IS NOT NULL
  AND image_provider IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM item_image WHERE item_image.item_id = item.id
  );
