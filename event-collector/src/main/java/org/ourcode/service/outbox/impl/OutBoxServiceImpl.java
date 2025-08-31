package org.ourcode.service.outbox.impl;

import lombok.RequiredArgsConstructor;
import org.ourcode.model.OutBoxEntity;
import org.ourcode.model.dto.OutBoxDto;
import org.ourcode.repository.OutBoxRepository;
import org.ourcode.service.outbox.OutBoxService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public void markAsProcessed(List<OutBoxDto> listOfOutbox) {
        List<OutBoxEntity> listOfOutboxEntity = mapListDtoToProcessedEntityList(listOfOutbox);
        outBoxRepository.saveAll(listOfOutboxEntity);
    }

    private List<OutBoxEntity> mapListDtoToProcessedEntityList(List<OutBoxDto> listOfOutboxDto) {
        return listOfOutboxDto.stream()
                .map(dto -> {
                    OutBoxEntity outBoxEntity = new OutBoxEntity();
                    outBoxEntity.setDeviceId(dto.getDeviceId());
                    outBoxEntity.setEventId(dto.getEventId());
                    outBoxEntity.setType(dto.getType());
                    outBoxEntity.setPayload(dto.getPayload());
                    outBoxEntity.setTimestamp(dto.getTimestamp());
                    outBoxEntity.setProcessed(true);
                    return outBoxEntity;
                })
                .toList();
    }
}
