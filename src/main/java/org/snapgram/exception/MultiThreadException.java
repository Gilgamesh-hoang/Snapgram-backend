package org.snapgram.exception;

public class MultiThreadException extends RuntimeException{
    public MultiThreadException(String message) {
        super(message);
    }
    public MultiThreadException(String message, Throwable cause) {
        super(message);
    }

}
