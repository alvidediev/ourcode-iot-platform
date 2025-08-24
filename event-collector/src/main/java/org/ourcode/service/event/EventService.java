package org.ourcode.service.event;

import org.ourcode.model.EventEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface EventService {

    /**
     * Батчевое сохранение ивентов в репозиторий
     */
    @Transactional
    List<EventEntity> saveAll(List<EventEntity> events);

    List<EventEntity> findAll();
}
