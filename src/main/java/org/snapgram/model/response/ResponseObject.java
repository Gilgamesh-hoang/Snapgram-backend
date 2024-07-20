package org.snapgram.model.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;

public class ResponseObject<T> extends ResponseEntity<ResponseObject.Payload<T>> {

    public ResponseObject(HttpStatus status, String message) {
        this(status, message, null);
    }

    public ResponseObject(HttpStatus status, String message, T data) {
        super(new Payload<T>(status.value(), message, data), status == HttpStatus.NO_CONTENT ? HttpStatus.OK : status);
    }

    public record Payload<T>(int status, String message, T data) implements Serializable {
    }
}
