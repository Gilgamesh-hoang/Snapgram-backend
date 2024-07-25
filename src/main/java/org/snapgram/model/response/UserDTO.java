package org.snapgram.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@Builder
public class UserDTO {
    private UUID id;
    private String nickname;
    private String fullName;
    private String email;
    private String activeCode;
    private Timestamp createdAt;
}
