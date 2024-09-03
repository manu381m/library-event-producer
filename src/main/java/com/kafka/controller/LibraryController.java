package com.kafka.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kafka.model.LibraryEvent;
import com.kafka.producer.LibraryEventProducer;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@RestController
@Slf4j
@RequestMapping(path = "/library")
public class LibraryController {

    private final LibraryEventProducer libraryEventProducer;

    public LibraryController(LibraryEventProducer libraryEventProducer) {
        this.libraryEventProducer = libraryEventProducer;
    }

    private static ResponseEntity<String> validateLibraryEvent(LibraryEvent libraryEvent) {
        if (libraryEvent.libraryEventId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please pass the library event Id");
        }
        if (libraryEvent.libraryEventType().equals(libraryEvent.libraryEventType().NEW)) {
            return ResponseEntity.status(HttpStatus.CREATED).body("Only UPDATE event type is supported");
        }
        return null;
    }

    @PostMapping(path = "/v1/create")
    public ResponseEntity<LibraryEvent> addBook(@RequestBody @Valid LibraryEvent libraryEvent) throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {
        log.info("Library event: {}", libraryEvent);
        //libraryEventProducer.sendLibraryEvent(libraryEvent);
        //libraryEventProducer.sendLibraryEvent1(libraryEvent);
        libraryEventProducer.sendLibraryEvent2(libraryEvent);
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }

    @PutMapping(path = "/v1/update")
    public ResponseEntity<?> updateBook(@RequestBody @Valid LibraryEvent libraryEvent) throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {
        log.info("Library event: {}", libraryEvent);
        //ResponseEntity<String> BAD_REQUEST = validateLibraryEvent(libraryEvent);
        //if (BAD_REQUEST != null) return BAD_REQUEST;
        libraryEventProducer.sendLibraryEvent2(libraryEvent);
        return ResponseEntity.status(HttpStatus.OK).body(libraryEvent);
    }

}
