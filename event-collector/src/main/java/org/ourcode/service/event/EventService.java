package org.ourcode.service.event;

import org.ourcode.model.EventEntity;

import java.util.List;

public interface EventService {

    /**
     * Батчевое сохранение ивентов в репозиторий
     */
    List<EventEntity> saveAll(List<EventEntity> events);

    EventEntity findByEventId(String eventId);
}
