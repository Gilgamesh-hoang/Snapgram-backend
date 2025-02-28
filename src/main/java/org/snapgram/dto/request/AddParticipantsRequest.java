package org.snapgram.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class AddParticipantsRequest {
    @NotNull
    private UUID conversationId;

    @NotEmpty
    private List<UUID> participantIds;
}