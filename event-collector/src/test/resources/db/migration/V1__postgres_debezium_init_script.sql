CREATE USER debezium WITH REPLICATION LOGIN PASSWORD 'debezium_password';

CREATE TABLE outbox_events
(
    eventId      TEXT PRIMARY KEY,
    deviceId     TEXT,
    timestamp    BIGINT,
    type         TEXT,
    payload      TEXT,
    is_processed BOOLEAN
);

CREATE PUBLICATION debezium_pub FOR TABLE outbox_events;

GRANT SELECT ON ALL TABLES IN SCHEMA public TO debezium;
GRANT USAGE ON SCHEMA public TO debezium;