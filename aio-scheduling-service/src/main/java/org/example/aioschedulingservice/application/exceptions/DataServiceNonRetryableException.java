package org.example.aioschedulingservice.application.exceptions;

public class DataServiceNonRetryableException extends DataServiceException {
    public DataServiceNonRetryableException(String message) {
        super(message);
    }

    public DataServiceNonRetryableException(String message, Throwable cause) {
        super(message, cause);
    }
}