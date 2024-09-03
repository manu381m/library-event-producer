package com.kafka.controllerAdvice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class LibraryControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleException(MethodArgumentNotValidException e) {
        var errorMsg = e.
                getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + " - " +fieldError.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining());
        log.info("Error message: {}", errorMsg);
        return new ResponseEntity<>(errorMsg, HttpStatus.BAD_REQUEST);
    }
}
