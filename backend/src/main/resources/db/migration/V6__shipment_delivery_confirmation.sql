ALTER TABLE shipment ADD COLUMN IF NOT EXISTS method VARCHAR(40) NOT NULL DEFAULT 'EXTERNAL_SHIPPING';
ALTER TABLE shipment ADD COLUMN IF NOT EXISTS proposer_delivery_confirmed_at TIMESTAMP;
ALTER TABLE shipment ADD COLUMN IF NOT EXISTS receiver_delivery_confirmed_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_shipment_trade_proposal
    ON shipment (trade_proposal_id);

CREATE INDEX IF NOT EXISTS idx_shipment_status
    ON shipment (status);
