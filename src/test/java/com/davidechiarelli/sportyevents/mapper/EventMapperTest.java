package com.davidechiarelli.sportyevents.mapper;

import com.davidechiarelli.sportyevents.dto.EventStatus;
import com.davidechiarelli.sportyevents.entity.EventStatusEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EventMapperTest {

    private final EventStatusMapper eventStatusMapper = new  EventStatusMapperImpl();

    @Test
    void shouldMapEventStatusToEventEntity() {

        EventStatus eventStatus = new EventStatus("1", true);

        EventStatusEntity eventStatusEntity = eventStatusMapper.toEventStatusEntity(eventStatus);

        assertNotNull(eventStatusEntity);
        assertThat(eventStatusEntity.getEventId()).isEqualTo("1");
        assertThat(eventStatusEntity.getStatus()).isTrue();
    }
}
