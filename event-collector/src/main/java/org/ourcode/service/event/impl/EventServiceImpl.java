package org.ourcode.service.event.impl;

import lombok.RequiredArgsConstructor;
import org.ourcode.model.EventEntity;
import org.ourcode.repository.EventRepository;
import org.ourcode.service.event.EventService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    @Transactional
    @Override
    public List<EventEntity> saveAll(List<EventEntity> events) {
        return eventRepository.saveAll(events);
    }

    @Override
    public List<EventEntity> findAll() {
        return eventRepository.findAll();
    }
}
