CREATE TABLE IF NOT EXISTS device_collector
(
    id          BIGSERIAL PRIMARY KEY,
    device_id   VARCHAR(100) NOT NULL UNIQUE,
    device_type VARCHAR(100) NOT NULL,
    created_at  BIGSERIAL    NOT NULL,
    meta        TEXT         NOT NULL
)