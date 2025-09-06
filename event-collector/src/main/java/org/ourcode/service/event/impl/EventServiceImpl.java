package org.ourcode.service.event.impl;

import lombok.RequiredArgsConstructor;
import org.ourcode.model.EventEntity;
import org.ourcode.repository.EventRepository;
import org.ourcode.service.event.EventService;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final CassandraTemplate cassandraTemplate;
    private final EventRepository eventRepository;

    @Override
    public List<EventEntity> saveAll(List<EventEntity> events) {
        int batchSize = 20;
        List<EventEntity> saved = new ArrayList<>();

        for (int i = 0; i < events.size(); i += batchSize) {
            List<EventEntity> chunk = events.subList(i, Math.min(i + batchSize, events.size()));
            var batchOps = cassandraTemplate.batchOps();
            chunk.forEach(batchOps::insert);
            batchOps.execute();
            saved.addAll(chunk);
        }

        return saved;
    }

    @Override
    public EventEntity findByEventId(String eventId) {
        return eventRepository.findById(eventId).orElseThrow();
    }
}
