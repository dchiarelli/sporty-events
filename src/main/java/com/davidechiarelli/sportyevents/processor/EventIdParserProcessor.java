package com.davidechiarelli.sportyevents.processor;

import com.davidechiarelli.sportyevents.entity.EventStatusEntity;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.kafka.KafkaConstants;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventIdParserProcessor implements Processor {

    @Override
    public void process(Exchange exchange) {

        EventStatusEntity eventStatus = exchange.getIn().getBody(EventStatusEntity.class);
        exchange.getIn().setHeader("eventId", eventStatus.getEventId());

        // Use Camel's exchange id as Kafka key
        exchange.getIn().setHeader(KafkaConstants.KEY, exchange.getExchangeId());
    }
}
