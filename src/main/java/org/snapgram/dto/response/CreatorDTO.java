package org.snapgram.dto.response;

import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
public class CreatorDTO implements Serializable {
    private UUID id;
    private String nickname;
    private String fullName;
    private String avatarUrl;
}
