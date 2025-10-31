package com.davidechiarelli.sportyevents.processor;

import com.davidechiarelli.sportyevents.dto.EventError;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.kafka.KafkaConstants;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;

@Component
@RequiredArgsConstructor
public class ExceptionProcessor implements Processor {

    private final ObjectMapper objectMapper;

    @Override
    public void process(Exchange exchange) throws Exception {

        Exception ex = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);

        String eventId = exchange.getIn().getHeader("eventId", String.class);

        if (eventId == null) {
            Object body = exchange.getIn().getBody();

            if (body != null) eventId = body.toString();
        }

        StringWriter sw = new StringWriter();
        if (ex != null) {
            ex.printStackTrace(new PrintWriter(sw));
        }

        EventError error = new EventError(
                eventId != null ? eventId : "unknown",
                ex != null ? ex.getMessage() : "null",
                ex != null ? ex.getClass().getName() : "UnknownException",
                sw.toString(),
                System.currentTimeMillis()
        );

        String json = objectMapper.writeValueAsString(error);
        exchange.getIn().setBody(json);
        exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");

        // Use Camel's exchange id as Kafka key for DLQ message
        exchange.getIn().setHeader(KafkaConstants.KEY, exchange.getExchangeId());
    }
}
