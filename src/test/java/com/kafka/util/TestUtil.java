package com.kafka.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafka.model.Book;
import com.kafka.model.LibraryEvent;
import com.kafka.model.LibraryEventType;

public class TestUtil {

    public static Book newBookRecord() {
        return new Book(123, "Kafka using springboot", "Dilip" );
    }

    public static Book newBookRecordWithInvalidValues() {
        return new Book(null, "Kafka using springboot", "" );
    }

    public static LibraryEvent addBookRecord() {
        return new LibraryEvent(null, LibraryEventType.NEW, newBookRecord());
    }

    public static LibraryEvent addBookRecordWithLibraryEventId() {
        return new LibraryEvent(123, LibraryEventType.NEW, newBookRecord());
    }

    public static LibraryEvent addBookRecordUpdate() {
        return new LibraryEvent(123, LibraryEventType.UPDATE, newBookRecord());
    }

    public static LibraryEvent addBookRecordUpdateWithNullLibraryEventId() {
        return new LibraryEvent(null, LibraryEventType.UPDATE, newBookRecord());
    }

    public static LibraryEvent addBookRecordWithInvalidBookId() {
        return new LibraryEvent(null, LibraryEventType.UPDATE, newBookRecordWithInvalidValues());
    }

    public static LibraryEvent parseAddBookRecord(ObjectMapper mapper, String json) {
        try {
            return  mapper.readValue(json, LibraryEvent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
