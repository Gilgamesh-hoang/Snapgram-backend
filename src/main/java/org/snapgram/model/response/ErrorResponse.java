package org.snapgram.model.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.sql.Timestamp;


@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Data
public class ErrorResponse implements Serializable {
    final Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    int status;
    String error;
    String message;
    String path;
}
