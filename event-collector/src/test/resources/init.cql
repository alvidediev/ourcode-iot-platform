CREATE KEYSPACE IF NOT EXISTS test_keyspace
            WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE test_keyspace;

CREATE TABLE IF NOT EXISTS device_event
(
    eventId   TEXT PRIMARY KEY,
    deviceId  TEXT,
    timestamp BIGINT,
    type      TEXT,
    payload   TEXT
);
