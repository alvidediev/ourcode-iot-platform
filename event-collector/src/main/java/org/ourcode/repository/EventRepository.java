package org.ourcode.repository;

import org.ourcode.model.EventEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends CassandraRepository<EventEntity, String> {
}
