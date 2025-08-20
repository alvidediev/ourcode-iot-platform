package org.ourcode.eventcollector.repository;

import org.ourcode.eventcollector.model.entity.EventEntity;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends ReactiveCassandraRepository<EventEntity, String> {
}
