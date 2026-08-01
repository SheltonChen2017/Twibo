CREATE TABLE broker_connection (
    id BIGINT NOT NULL AUTO_INCREMENT,
    owner_id BIGINT NOT NULL,
    provider VARCHAR(20) NOT NULL,
    environment VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL,
    external_account_label VARCHAR(80),
    granted_scopes VARCHAR(255) NOT NULL DEFAULT '',
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_broker_connection_owner_provider_env
        UNIQUE (owner_id, provider, environment),
    CONSTRAINT fk_broker_connection_owner
        FOREIGN KEY (owner_id) REFERENCES app_user (id)
);

CREATE INDEX idx_broker_connection_owner_status
    ON broker_connection (owner_id, status);
