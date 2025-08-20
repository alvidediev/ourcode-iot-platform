CREATE TABLE IF NOT EXISTS event_keyspace.events
(
    eventId   text PRIMARY KEY,
    deviceId  text,
    timestamp bigint,
    type      text,
    payload   text
);
