package org.ourcode.service.outbox;

import org.ourcode.model.OutBoxEntity;
import org.ourcode.model.dto.OutBoxDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OutBoxService {
    @Transactional
    Iterable<OutBoxEntity> saveAll(List<OutBoxEntity> listOfOutbox);

    List<OutBoxEntity> findAllUnprocessed();

    List<OutBoxEntity> findAllProcessed();

    void markAsProcessed(List<OutBoxDto> listOfOutbox);
}
