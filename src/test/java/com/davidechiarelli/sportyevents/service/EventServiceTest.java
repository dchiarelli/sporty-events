package com.davidechiarelli.sportyevents.service;

import com.davidechiarelli.sportyevents.dto.EventStatus;
import com.davidechiarelli.sportyevents.mapper.EventStatusMapper;
import com.davidechiarelli.sportyevents.repository.EventStatusRepository;
import com.davidechiarelli.sportyevents.services.EventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventStatusRepository eventStatusRepository;

    @Mock
    private EventStatusMapper eventStatusMapper;

    @InjectMocks
    private EventService eventService;

    @Test
    void shouldSaveEvent() {
        EventStatus eventStatus = new EventStatus("1", true);

        assertThatNoException().isThrownBy(() -> eventService.saveEventStatus(eventStatus));}
}
