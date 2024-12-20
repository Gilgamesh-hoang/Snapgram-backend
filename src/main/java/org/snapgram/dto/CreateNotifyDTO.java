package org.snapgram.dto;

import lombok.Builder;
import lombok.Data;
import org.snapgram.enums.NotificationType;

import java.util.UUID;

@Data
@Builder
public class CreateNotifyDTO {
    private NotificationType type;
    private UUID entityId;
    private UUID actorId;
}
