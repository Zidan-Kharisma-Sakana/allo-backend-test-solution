package com.allobank.splitbill.exception;

import com.allobank.splitbill.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<Object> application(ApplicationException failure) {
        if (failure.getStatus() >= 500) {
            log.error("Application failed");
        } else {
            log.info("Request rejected");
        }
        return error(HttpStatusCode.valueOf(failure.getStatus()), failure.getMessage(), HttpHeaders.EMPTY);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception failure, Object body,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String message = "Invalid request";
        if (status.is5xxServerError()) {
            log.error("Request failed", failure);
            message = "Something went wrong, try again";
        } else {
            log.info("Invalid HTTP request");
        }
        return error(status, message, headers);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> unexpected(Exception failure, WebRequest request) {
        log.error("Request failed", failure);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong, try again", HttpHeaders.EMPTY);
    }

    private ResponseEntity<Object> error(HttpStatusCode status, String message,
            HttpHeaders headers) {
        ErrorResponse body = new ErrorResponse(status.value(), message);
        return new ResponseEntity<>(body, headers, status);
    }
}
