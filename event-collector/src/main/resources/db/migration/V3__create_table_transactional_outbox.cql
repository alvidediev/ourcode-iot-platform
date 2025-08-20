CREATE TABLE IF NOT EXISTS event_keyspace.transactional_outbox
(
    eventId   text PRIMARY KEY,
    deviceId  text,
    timestamp bigint,
    type      text,
    payload   text,
    status   text
);
