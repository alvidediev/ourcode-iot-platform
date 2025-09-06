package org.ourcode.mapper;

import avro.DeviceEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.ourcode.model.EventEntity;

@Mapper(componentModel = "spring")
public interface EventMapper {
    @Mapping(source = "eventId", target = "eventId")
    @Mapping(source = "deviceId", target = "deviceId")
    @Mapping(source = "timestamp", target = "timestamp")
    @Mapping(source = "payload", target = "payload")
    @Mapping(source = "type", target = "type")
    EventEntity deviceEventToEntity(DeviceEvent deviceEvent);
}
