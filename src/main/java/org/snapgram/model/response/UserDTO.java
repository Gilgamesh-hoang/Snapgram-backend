package org.snapgram.model.response;

import lombok.Data;

import java.util.UUID;

@Data
public class UserDTO {
    private UUID id;
    private String nickName;
    private String email;
}
