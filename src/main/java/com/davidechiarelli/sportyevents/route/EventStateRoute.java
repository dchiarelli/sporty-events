package com.davidechiarelli.sportyevents.route;

import com.davidechiarelli.sportyevents.dto.EventScore;
import com.davidechiarelli.sportyevents.processor.EventIdParserProcessor;
import com.davidechiarelli.sportyevents.processor.EventStatusesProcessor;
import com.davidechiarelli.sportyevents.processor.ExceptionProcessor;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import static org.apache.camel.LoggingLevel.ERROR;
import static org.apache.camel.model.dataformat.JsonLibrary.Jackson;

@Component
@RequiredArgsConstructor
public class EventStateRoute extends RouteBuilder {

    private final ExceptionProcessor exceptionProcessor;
    private final EventStatusesProcessor eventStatusesProcessor;
    private final EventIdParserProcessor eventIdParserProcessor;

    @Override
    public void configure() {

        onException(Exception.class)
                .routeId("route-exception")
                .maximumRedeliveries("{{event-retry-max-redeliveries}}")
                .redeliveryDelay("{{event-retry-redelivery-delay}}")
                .retryAttemptedLogLevel(org.apache.camel.LoggingLevel.WARN)
                .handled(true)
                .process(exceptionProcessor)
                .to("direct:sendEventScoreToDql");

        from("direct:callEventScoreService")
            .routeId("route-call-event-score-service")
            .setHeader("CamelHttpMethod", constant("GET"))
            .setHeader("CamelHttpUri", simple("{{event-scores-url}}/${header.eventId}"))
            .toD("${header.CamelHttpUri}")
            .log("Event result response for eventId ${header.eventId}: ${body}")
            .unmarshal()
            .json(Jackson, EventScore.class)
            .to("bean-validator://validate")
            .log("Valid EventScore: ${body}")
            .marshal()
            .json(Jackson);

        from("direct:sendEventScore")
            .routeId("route-send-event-score")
            .to("kafka:{{kafka.topic}}?brokers={{kafka.bootstrap-servers}}")
            .log("Sent EventScore for eventId ${header.eventId} to Kafka topic {{kafka.topic}}");

        from("direct:sendEventScoreToDql")
                .routeId("route-send-event-score-to-dlq")
                .log("Before kafka invoking")
                .to("kafka:{{kafka.dlq-topic}}?brokers={{kafka.bootstrap-servers}}")
                .log(ERROR, "Sent event to DLQ after failed retries: ${body}");

        from("scheduler://eventState?delay={{event-delay}}")
            .routeId("route-scheduler")
            .process(eventStatusesProcessor)
            .split(body())
                .parallelProcessing()
                .process(eventIdParserProcessor)
                .to("direct:callEventScoreService")
                .to("direct:sendEventScore")
            .end();
    }
}
