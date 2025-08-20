package org.ourcode.eventcollector.repository;

import org.ourcode.eventcollector.model.entity.EventOutboxEntity;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface OutboxRepository extends ReactiveCassandraRepository<EventOutboxEntity, String> {
    Flux<EventOutboxEntity> findByStatus(String status);
}
