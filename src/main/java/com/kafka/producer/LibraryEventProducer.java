package com.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafka.model.LibraryEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class LibraryEventProducer {

    private final KafkaTemplate<Integer, String> kafkaTemplate;
    private final ObjectMapper mapper;
    @Value("${spring.kafka.topic}")
    private String topic;

    public LibraryEventProducer(KafkaTemplate<Integer, String> kafkaTemplate, ObjectMapper mapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.mapper = mapper;
    }

    public ProducerRecord<Integer, String> buildProducerRecord(Integer key, String value) {
        List<Header> recordHeaders= List.of(new RecordHeader("event-source", "scanner".getBytes()));
        return new ProducerRecord<>(topic, null, key, value, recordHeaders);
    }


    //Async call
    public CompletableFuture<SendResult<Integer, String>> sendLibraryEvent(LibraryEvent libraryEvent) throws JsonProcessingException {
        var key = libraryEvent.libraryEventId();
        var value = mapper.writeValueAsString(libraryEvent);

        var completableFuture = kafkaTemplate.send(topic, key, value);

        return completableFuture.whenComplete((sendResult, throwable) -> {
                    if (throwable != null) {
                        handleFailure(key, value, throwable);
                    } else {
                        handleSuccess(key, value, sendResult);
                    }
                }
        );
    }

    //Sync Call
    public SendResult<Integer, String> sendLibraryEvent1(LibraryEvent libraryEvent) throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {
        var key = libraryEvent.libraryEventId();
        var value = mapper.writeValueAsString(libraryEvent);
        var sendResult = kafkaTemplate.send(topic, key, value).get(3, TimeUnit.SECONDS);
        handleSuccess(key, value, sendResult);
        return sendResult;

    }

    //approach3 by producer record
    public CompletableFuture<SendResult<Integer, String>> sendLibraryEvent2(LibraryEvent libraryEvent) throws JsonProcessingException {
        var key = libraryEvent.libraryEventId();
        var value = mapper.writeValueAsString(libraryEvent);
        var producerRecord = buildProducerRecord(key, value);
        var completableFuture = kafkaTemplate.send(producerRecord);

        return completableFuture.whenComplete((sendResult, throwable) -> {
                    if (throwable != null) {
                        handleFailure(key, value, throwable);
                    } else {
                        handleSuccess(key, value, sendResult);
                    }
                }
        );
    }

    public void handleSuccess(Integer key, String value, SendResult<Integer, String> sendResult) {
        log.info("Message sent successfully for the key: {} and the value: {}, partition is {}", key, value, sendResult.getRecordMetadata().partition());
    }

    public void handleFailure(Integer key, String value, Throwable throwable) {
        log.error("Error sending the message and the exception: {}", throwable.getMessage(), throwable);
    }


}
