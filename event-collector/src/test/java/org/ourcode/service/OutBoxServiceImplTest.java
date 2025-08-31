package org.ourcode.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ourcode.model.OutBoxEntity;
import org.ourcode.model.dto.OutBoxDto;
import org.ourcode.repository.OutBoxRepository;
import org.ourcode.service.outbox.impl.OutBoxServiceImpl;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutBoxServiceImplTest {

    @Mock
    private OutBoxRepository outBoxRepository;

    @InjectMocks
    private OutBoxServiceImpl outBoxService;

    @Test
    void testSaveAll() {
        List<OutBoxEntity> outboxEntities = Arrays.asList(
                createOutBoxEntity("event-1", "device-1", false),
                createOutBoxEntity("event-2", "device-2", false)
        );

        when(outBoxRepository.saveAll(anyList())).thenReturn(outboxEntities);

        // Act
        Iterable<OutBoxEntity> result = outBoxService.saveAll(outboxEntities);

        // Assert
        assertNotNull(result);
        verify(outBoxRepository, times(1)).saveAll(outboxEntities);
    }

    @Test
    void testFindAllUnprocessed() {
        List<OutBoxEntity> unprocessedEntities = Arrays.asList(
                createOutBoxEntity("event-1", "device-1", false),
                createOutBoxEntity("event-2", "device-2", false)
        );

        when(outBoxRepository.findAllByProcessedFalse()).thenReturn(unprocessedEntities);

        List<OutBoxEntity> result = outBoxService.findAllUnprocessed();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertFalse(result.getFirst().isProcessed());
        verify(outBoxRepository, times(1)).findAllByProcessedFalse();
    }

    @Test
    void testMarkAsProcessed() {
        String deviceId = "device-1";

        List<OutBoxEntity> entities = List.of(
                createOutBoxEntity("event-1", deviceId, false),
                createOutBoxEntity("event-2", deviceId, false)
        );

        when(outBoxRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<OutBoxDto> dtos = List.of(
                createOutBoxDto("event-1", deviceId, false),
                createOutBoxDto("event-2", deviceId, false)
        );

        outBoxService.markAsProcessed(dtos);

        verify(outBoxRepository, times(1)).saveAll(anyList());
    }

    private OutBoxEntity createOutBoxEntity(String eventId, String deviceId, boolean processed) {
        OutBoxEntity entity = new OutBoxEntity();
        entity.setEventId(eventId);
        entity.setDeviceId(deviceId);
        entity.setTimestamp(System.currentTimeMillis());
        entity.setType("temperature");
        entity.setPayload("{\"value\": 25}");
        entity.setProcessed(processed);
        return entity;
    }

    private OutBoxDto createOutBoxDto(String eventId, String deviceId, boolean processed) {
        OutBoxDto entity = new OutBoxDto();
        entity.setEventId(eventId);
        entity.setDeviceId(deviceId);
        entity.setTimestamp(System.currentTimeMillis());
        entity.setType("temperature");
        entity.setPayload("{\"value\": 25}");
        entity.setProcessed(processed);
        return entity;
    }
}
