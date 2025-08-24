package org.ourcode.service.outbox.impl;

import lombok.RequiredArgsConstructor;
import org.ourcode.model.OutBoxEntity;
import org.ourcode.repository.OutBoxRepository;
import org.ourcode.service.outbox.OutBoxService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class OutBoxServiceImpl implements OutBoxService {

    private final OutBoxRepository outBoxRepository;

    @Transactional
    @Override
    public Iterable<OutBoxEntity> saveAll(List<OutBoxEntity> listOfOutbox) {
        return outBoxRepository.saveAll(listOfOutbox);
    }

    @Override
    public List<OutBoxEntity> findAllUnprocessed() {
        return outBoxRepository.findAllByProcessedFalse();
    }

    @Override
    public List<OutBoxEntity> findAllProcessed() {
        return outBoxRepository.findAllByProcessedTrue();
    }

    @Override
    public void markAsProcessed(String deviceId) {
        List<OutBoxEntity> listOfOutboxEntity = outBoxRepository.findAllByDeviceId(deviceId);
        listOfOutboxEntity.forEach(outBoxEntity -> {
            outBoxEntity.setProcessed(true);
            outBoxRepository.save(outBoxEntity);
        });

    }
}
