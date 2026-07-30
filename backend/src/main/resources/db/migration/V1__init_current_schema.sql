CREATE TABLE usuario (
    id BIGSERIAL PRIMARY KEY,
    firstname VARCHAR(50) NOT NULL,
    lastname VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(15) NOT NULL,
    password VARCHAR(255) NOT NULL,
    address VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    description VARCHAR(255)
);

CREATE TABLE item (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL,
    category_id BIGINT NOT NULL,
    condition VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    image_key VARCHAR(255),
    image_provider VARCHAR(20),
    CONSTRAINT fk_item_category FOREIGN KEY (category_id) REFERENCES category(id),
    CONSTRAINT fk_item_usuario FOREIGN KEY (user_id) REFERENCES usuario(id),
    CONSTRAINT chk_item_image_key_provider CHECK ((image_key IS NULL) = (image_provider IS NULL))
);

CREATE TABLE trade_proposal (
    id BIGSERIAL PRIMARY KEY,
    version INTEGER,
    status VARCHAR(30) NOT NULL,
    offered_item_id BIGINT NOT NULL,
    requested_item_id BIGINT NOT NULL,
    proposer_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_trade_proposal_offered_item FOREIGN KEY (offered_item_id) REFERENCES item(id),
    CONSTRAINT fk_trade_proposal_requested_item FOREIGN KEY (requested_item_id) REFERENCES item(id),
    CONSTRAINT fk_trade_proposal_proposer FOREIGN KEY (proposer_id) REFERENCES usuario(id),
    CONSTRAINT fk_trade_proposal_receiver FOREIGN KEY (receiver_id) REFERENCES usuario(id)
);

CREATE UNIQUE INDEX uq_trade_proposal_active_pair
    ON trade_proposal (offered_item_id, requested_item_id)
    WHERE status = 'PENDING';

CREATE TABLE shipment (
    id BIGSERIAL PRIMARY KEY,
    trade_proposal_id BIGINT UNIQUE,
    initiator_address VARCHAR(255) NOT NULL,
    receive_address VARCHAR(255) NOT NULL,
    delivery_date TIMESTAMP,
    status VARCHAR(30) NOT NULL,
    tracking_code VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    prepared_at TIMESTAMP,
    shipped_at TIMESTAMP,
    delivered_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    CONSTRAINT fk_shipment_trade_proposal FOREIGN KEY (trade_proposal_id) REFERENCES trade_proposal(id)
);

CREATE UNIQUE INDEX uq_shipment_tracking_code
    ON shipment (tracking_code)
    WHERE tracking_code IS NOT NULL;

CREATE TABLE rating (
    id BIGSERIAL PRIMARY KEY,
    trade_proposal_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    reviewed_user_id BIGINT NOT NULL,
    score INTEGER NOT NULL,
    comment VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_rating_trade_proposal FOREIGN KEY (trade_proposal_id) REFERENCES trade_proposal(id),
    CONSTRAINT fk_rating_reviewer FOREIGN KEY (reviewer_id) REFERENCES usuario(id),
    CONSTRAINT fk_rating_reviewed_user FOREIGN KEY (reviewed_user_id) REFERENCES usuario(id),
    CONSTRAINT chk_rating_score_range CHECK (score BETWEEN 1 AND 5),
    CONSTRAINT uq_rating_trade_proposal_reviewer UNIQUE (trade_proposal_id, reviewer_id)
);

