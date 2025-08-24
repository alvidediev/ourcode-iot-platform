CREATE KEYSPACE IF NOT EXISTS event_keyspace
            WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE event_keyspace;

CREATE TABLE IF NOT EXISTS device_event
(
    eventId   TEXT PRIMARY KEY,
    deviceId  TEXT,
    timestamp BIGINT,
    type      TEXT,
    payload   TEXT
);
