CREATE KEYSPACE IF NOT EXISTS event_keyspace
    WITH replication = {
        'class': 'SimpleStrategy',
        'replication_factor': 1
        };