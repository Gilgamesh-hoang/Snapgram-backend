package org.snapgram.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.snapgram.enums.ConversationType;

import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ConversationDTO {
    UUID id;
    String name;
    ConversationType type;
    String avatar;
    CreatorDTO owner;
}
