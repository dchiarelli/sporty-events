package com.davidechiarelli.sportyevents.route;

import com.davidechiarelli.sportyevents.dto.EventError;
import com.davidechiarelli.sportyevents.dto.EventScore;
import com.davidechiarelli.sportyevents.entity.EventStatusEntity;
import com.davidechiarelli.sportyevents.repository.EventStatusRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.apache.camel.builder.AdviceWith.adviceWith;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.Mockito.when;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

@SpringBootTest
@CamelSpringBootTest
@AutoConfigureWireMock(port = 0, stubs = {"classpath:/mappings"})
@ActiveProfiles("test")
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
class EventStateRouteTest {


    @MockitoBean
    private EventStatusRepository eventStatusRepository;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    ProducerTemplate producerTemplate;

    @Autowired
    CamelContext camelContext;

    @Autowired
    private ObjectMapper objectMapper;

    private Consumer<String, String> consumer;

    @BeforeEach
    void setUp() throws Exception {

        adviceWith(camelContext, "route-scheduler", routeBuilder ->
                routeBuilder.replaceFromWith("direct:triggerScheduler"));

        camelContext.start();

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafka);
        consumerProps.put(AUTO_OFFSET_RESET_CONFIG, "earliest");

        consumer = new KafkaConsumer<>(consumerProps, new StringDeserializer(), new StringDeserializer());

        consumer.subscribe(List.of("eventScores", "eventScores_dlq"));
    }


    @Test
    void shouldIngestEventStatus() throws Exception {

        when(eventStatusRepository.findAllByStatusIsTrue()).thenReturn(List.of(new EventStatusEntity("1", true)));

        producerTemplate.sendBody("direct:triggerScheduler", null);

        var kafkaRecord = KafkaTestUtils.getSingleRecord(consumer, "eventScores", Duration.ofSeconds(5));
        assertThat(kafkaRecord).isNotNull();

        var eventScore = objectMapper.readValue(kafkaRecord.value(), EventScore.class);
        assertThat(eventScore.eventId()).isEqualTo("1");
        assertThat(eventScore.currentScore()).isEqualTo("10:1");
    }

    @Test
    void shouldNotIngestEventStatusForException() throws Exception {

        when(eventStatusRepository.findAllByStatusIsTrue()).thenReturn(List.of(new EventStatusEntity("2", true)));

        producerTemplate.sendBody("direct:triggerScheduler", null);

        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> KafkaTestUtils.getSingleRecord(consumer, "eventScores", Duration.ofSeconds(5)));

        var kafkaRecord = KafkaTestUtils.getSingleRecord(consumer, "eventScores_dlq", Duration.ofSeconds(5));
        assertThat(kafkaRecord).isNotNull();

        var eventError = objectMapper.readValue(kafkaRecord.value(), EventError.class);
        assertThat(eventError.eventId()).isEqualTo("2");
        assertThat(eventError.errorMessage()).contains("HTTP operation failed invoking");
    }
}
