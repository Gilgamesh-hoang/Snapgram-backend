package org.snapgram.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreatorDTO implements Serializable {
    private UUID id;
    private String nickname;
    private String fullName;
    private String avatarUrl;
}
