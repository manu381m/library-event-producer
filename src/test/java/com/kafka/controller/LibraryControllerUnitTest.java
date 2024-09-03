package com.kafka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafka.model.LibraryEvent;
import com.kafka.producer.LibraryEventProducer;
import com.kafka.util.TestUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LibraryController.class)
class LibraryControllerUnitTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    LibraryEventProducer producer;

    @Test
    void addBook() throws Exception {

        var json = mapper.writeValueAsString(TestUtil.newBookRecord());
        when(producer.sendLibraryEvent2(isA(LibraryEvent.class))).thenReturn(null);

        mvc
                .perform(MockMvcRequestBuilders.post("/library/v1/create").content(json).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void addBookWithInvalidValues() throws Exception {

        var json = mapper.writeValueAsString(TestUtil.newBookRecordWithInvalidValues());
        when(producer.sendLibraryEvent2(isA(LibraryEvent.class))).thenReturn(null);

        mvc
                .perform(MockMvcRequestBuilders.post("/library/v1/create").content(json).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }
}