package org.example.aioschedulingservice.application.exceptions;

public class DataServiceException extends RuntimeException {
    public DataServiceException(String message) {
        super(message);
    }

    public DataServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
