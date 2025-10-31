package com.davidechiarelli.sportyevents.mapper;

import com.davidechiarelli.sportyevents.dto.EventStatus;
import com.davidechiarelli.sportyevents.entity.EventStatusEntity;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EventStatusMapper {

    EventStatusEntity toEventStatusEntity(EventStatus eventStatus);
}
