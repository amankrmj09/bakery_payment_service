CREATE TABLE payment_transactions
(
    id                     UUID           NOT NULL,
    payment_id             UUID           NOT NULL,
    transaction_type       VARCHAR(255)   NOT NULL,
    status                 VARCHAR(255)   NOT NULL,
    amount                 DECIMAL(12, 2) NOT NULL,
    currency_code          VARCHAR(3)     NOT NULL,
    gateway_transaction_id VARCHAR(100),
    gateway_response       TEXT,
    gateway_raw_response   TEXT,
    failure_reason         TEXT,
    failure_code           VARCHAR(50),
    created_at             TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    processed_at           TIMESTAMP WITHOUT TIME ZONE,
    description            TEXT,
    metadata               TEXT,
    CONSTRAINT pk_payment_transactions PRIMARY KEY (id)
);

CREATE TABLE payments
(
    id                      UUID           NOT NULL,
    payment_reference       VARCHAR(50)    NOT NULL,
    order_id                UUID           NOT NULL,
    user_id                 UUID           NOT NULL,
    payment_method          VARCHAR(255)   NOT NULL,
    payment_gateway         VARCHAR(255)   NOT NULL,
    status                  VARCHAR(255)   NOT NULL,
    amount                  DECIMAL(12, 2) NOT NULL,
    currency_code           VARCHAR(3)     NOT NULL,
    description             TEXT,
    card_last_four          VARCHAR(4),
    card_brand              VARCHAR(20),
    card_type               VARCHAR(20),
    digital_wallet_provider VARCHAR(50),
    bank_name               VARCHAR(100),
    gateway_payment_id      VARCHAR(100),
    external_transaction_id VARCHAR(100),
    gateway_response        TEXT,
    gateway_raw_response    TEXT,
    failure_reason          TEXT,
    failure_code            VARCHAR(50),
    retry_count             INTEGER,
    last_retry_at           TIMESTAMP WITHOUT TIME ZONE,
    created_at              TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at              TIMESTAMP WITHOUT TIME ZONE,
    authorized_at           TIMESTAMP WITHOUT TIME ZONE,
    captured_at             TIMESTAMP WITHOUT TIME ZONE,
    failed_at               TIMESTAMP WITHOUT TIME ZONE,
    cancelled_at            TIMESTAMP WITHOUT TIME ZONE,
    expires_at              TIMESTAMP WITHOUT TIME ZONE,
    gateway_fee             DECIMAL(8, 2),
    net_amount              DECIMAL(12, 2),
    settlement_date         TIMESTAMP WITHOUT TIME ZONE,
    metadata                TEXT,
    notes                   TEXT,
    CONSTRAINT pk_payments PRIMARY KEY (id)
);

CREATE TABLE refunds
(
    id                   UUID           NOT NULL,
    refund_reference     VARCHAR(50)    NOT NULL,
    payment_id           UUID           NOT NULL,
    status               VARCHAR(255)   NOT NULL,
    amount               DECIMAL(12, 2) NOT NULL,
    currency_code        VARCHAR(3)     NOT NULL,
    reason               TEXT,
    gateway_refund_id    VARCHAR(100),
    gateway_response     TEXT,
    gateway_raw_response TEXT,
    failure_reason       TEXT,
    failure_code         VARCHAR(50),
    requested_by         UUID           NOT NULL,
    approved_by          UUID,
    created_at           TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at           TIMESTAMP WITHOUT TIME ZONE,
    processed_at         TIMESTAMP WITHOUT TIME ZONE,
    completed_at         TIMESTAMP WITHOUT TIME ZONE,
    failed_at            TIMESTAMP WITHOUT TIME ZONE,
    notes                TEXT,
    metadata             TEXT,
    CONSTRAINT pk_refunds PRIMARY KEY (id)
);

ALTER TABLE payments
    ADD CONSTRAINT uc_payments_payment_reference UNIQUE (payment_reference);

ALTER TABLE refunds
    ADD CONSTRAINT uc_refunds_refund_reference UNIQUE (refund_reference);

CREATE INDEX idx_external_transaction ON payments (external_transaction_id);

CREATE INDEX idx_payment_date ON payments (created_at);

CREATE INDEX idx_payment_gateway ON payments (payment_gateway);

CREATE INDEX idx_payment_method ON payments (payment_method);

CREATE INDEX idx_payment_order ON payments (order_id);

CREATE INDEX idx_payment_reference ON payments (payment_reference);

CREATE INDEX idx_payment_status ON payments (status);

CREATE INDEX idx_refund_date ON refunds (created_at);

CREATE INDEX idx_refund_gateway ON refunds (gateway_refund_id);

CREATE INDEX idx_refund_reference ON refunds (refund_reference);

CREATE INDEX idx_refund_status ON refunds (status);

CREATE INDEX idx_transaction_date ON payment_transactions (created_at);

CREATE INDEX idx_transaction_gateway ON payment_transactions (gateway_transaction_id);

CREATE INDEX idx_transaction_status ON payment_transactions (status);

CREATE INDEX idx_transaction_type ON payment_transactions (transaction_type);

ALTER TABLE payment_transactions
    ADD CONSTRAINT FK_PAYMENT_TRANSACTIONS_ON_PAYMENT FOREIGN KEY (payment_id) REFERENCES payments (id);

CREATE INDEX idx_transaction_payment ON payment_transactions (payment_id);

ALTER TABLE refunds
    ADD CONSTRAINT FK_REFUNDS_ON_PAYMENT FOREIGN KEY (payment_id) REFERENCES payments (id);

CREATE INDEX idx_refund_payment ON refunds (payment_id);
