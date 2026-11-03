package com.allobank.splitbill.exception;

public class ApplicationException extends RuntimeException {
    private final int status;

    public ApplicationException(int status, String message) {
        super(message);
        this.status = status;
    }

    public static ApplicationException invalidRequest() {
        return new ApplicationException(400, "Invalid request");
    }

    public int getStatus() {
        return status;
    }
}
