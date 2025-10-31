package com.davidechiarelli.sportyevents.controller;

import com.davidechiarelli.sportyevents.dto.EventStatus;
import com.davidechiarelli.sportyevents.services.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping(path = "/events/status")
    public ResponseEntity<Void> updateEventStatus(@Valid @RequestBody EventStatus eventStatus) {

        log.info("Received event status: {}", eventStatus);

        eventService.saveEventStatus(eventStatus);

        return ResponseEntity.ok().build();
    }
}
