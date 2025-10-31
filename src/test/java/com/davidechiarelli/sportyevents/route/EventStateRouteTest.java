package com.davidechiarelli.sportyevents.route;

import com.davidechiarelli.sportyevents.entity.EventStatusEntity;
import com.davidechiarelli.sportyevents.repository.EventStatusRepository;
import org.apache.camel.CamelContext;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.engine.DefaultShutdownStrategy;
import org.apache.camel.spi.ShutdownStrategy;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.apache.camel.builder.AdviceWith.adviceWith;
import static org.apache.camel.test.junit5.TestSupport.getMockEndpoint;
import static org.mockito.Mockito.when;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

@SpringBootTest
@CamelSpringBootTest
@AutoConfigureWireMock(port = 0, stubs = {"classpath:/mappings"})
@ActiveProfiles("test")
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
class EventStateRouteTest {

    @Autowired
    protected CamelContext camelContext;

    @MockitoBean
    private EventStatusRepository eventStatusRepository;

    private MockEndpoint mockedKafka;
    private MockEndpoint mockedDlqKafka;

    @BeforeEach
    void setUp() throws Exception {

        ShutdownStrategy shutdownStrategy = new DefaultShutdownStrategy();
        shutdownStrategy.setTimeout(10);
        shutdownStrategy.setShutdownNowOnTimeout(true);
        shutdownStrategy.setLogInflightExchangesOnTimeout(true);
        shutdownStrategy.setSuppressLoggingOnTimeout(true);
        camelContext.setShutdownStrategy(shutdownStrategy);

        adviceWith(camelContext, "route-send-event-score", routeBuilder ->
                routeBuilder
                        .weaveByToUri("kafka:*")
                        .replace()
                        .to("mock:kafka")
        );

        adviceWith(camelContext, "route-send-event-score-to-dlq", routeBuilder ->
                routeBuilder
                        .weaveByToUri("kafka:*")
                        .replace()
                        .to("mock:kafka-dlq")
        );

        mockedKafka = getMockEndpoint(camelContext, "mock:kafka", true);
        mockedDlqKafka = getMockEndpoint(camelContext, "mock:kafka-dlq", true);
    }

    @AfterEach
    void cleanUp() {

        camelContext.stop();
    }

    @Test
    void shouldIngestEventStatus() throws InterruptedException {

        when(eventStatusRepository.findAllByStatusIsTrue()).thenReturn(List.of(new EventStatusEntity("1", true)));

        mockedDlqKafka.expectedMinimumMessageCount(1);
        mockedKafka.expectedBodiesReceived("{\"eventId\":\"1\",\"currentScore\":\"10:1\"}");
        mockedKafka.assertIsSatisfied();
    }

    @Test
    void shouldNotIngestEventStatusForException() throws InterruptedException {

        when(eventStatusRepository.findAllByStatusIsTrue()).thenReturn(List.of(new EventStatusEntity("2", true)));

        mockedDlqKafka.expectedMinimumMessageCount(1);
        mockedDlqKafka.message(0).body().contains("{\"eventId\":\"2\",\"errorMessage\":\"HTTP operation failed invoking");
        mockedDlqKafka.assertIsSatisfied();
    }

}
