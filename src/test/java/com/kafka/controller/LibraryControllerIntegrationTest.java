package com.kafka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafka.model.LibraryEvent;
import com.kafka.util.TestUtil;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = "library-events")
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.admin.properties.bootstrap-server=${spring.embedded.kafka.brokers}"})
class LibraryControllerIntegrationTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    ObjectMapper objectMapper;

    private Consumer<Integer, String> consumer;

    @BeforeEach
    void setUp() {
        var configs = new HashMap<>(KafkaTestUtils.consumerProps("group1", "true", embeddedKafka));
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        consumer = new DefaultKafkaConsumerFactory<>(configs, new IntegerDeserializer(), new StringDeserializer()).createConsumer();
        embeddedKafka.consumeFromAllEmbeddedTopics(consumer);
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

    @Test
    void addBook() {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content_type", MediaType.APPLICATION_JSON_VALUE.toString());
        var httpEntity = new HttpEntity<>(TestUtil.addBookRecord(), headers);

        var responseEntity = restTemplate.exchange("/library/v1/create", HttpMethod.POST, httpEntity, LibraryEvent.class);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

        var consumerRecords = KafkaTestUtils.getRecords(consumer);
        assert consumerRecords.count() == 1;
        consumerRecords.forEach(record -> {
            var libraryEvent = TestUtil.parseAddBookRecord(objectMapper, record.value());
            System.out.println("Library event: " + libraryEvent);
            assertEquals(libraryEvent, TestUtil.addBookRecord());
        });

    }
}