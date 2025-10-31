package com.davidechiarelli.sportyevents.processor;

import com.davidechiarelli.sportyevents.entity.EventStatusEntity;
import com.davidechiarelli.sportyevents.repository.EventStatusRepository;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EventStatusesProcessor implements Processor {

    private final EventStatusRepository eventStatusRepository;

    @Override
    public void process(Exchange exchange) {

        List<EventStatusEntity> eventStatusEntities = eventStatusRepository.findAllByStatusIsTrue();

        exchange.getIn().setBody(eventStatusEntities);
    }
}
