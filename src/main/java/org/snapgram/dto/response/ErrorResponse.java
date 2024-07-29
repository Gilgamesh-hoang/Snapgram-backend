package org.snapgram.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.sql.Timestamp;


@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Data
public class ErrorResponse implements Serializable {
    final Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    String error;
    String message;
    String path;
}
