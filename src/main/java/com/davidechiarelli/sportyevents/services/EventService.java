package com.davidechiarelli.sportyevents.services;

import com.davidechiarelli.sportyevents.dto.EventStatus;
import com.davidechiarelli.sportyevents.mapper.EventStatusMapper;
import com.davidechiarelli.sportyevents.repository.EventStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventStatusRepository eventStatusRepository;
    private final EventStatusMapper eventStatusMapper;

    @SneakyThrows
    public void saveEventStatus(EventStatus eventStatus) {

        eventStatusRepository.save(eventStatusMapper.toEventStatusEntity(eventStatus));

        log.info("Event {} successfully saved.", eventStatus);
    }
}
