package org.snapgram.dto.response;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

@Data
@Builder
public class UserDTO implements Serializable {
    private UUID id;
    private String nickname;
    private String fullName;
    private String email;
    private String activeCode;
    private Timestamp createdAt;
}
