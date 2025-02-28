package org.snapgram.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.snapgram.dto.CloudinaryMedia;

import java.util.UUID;

@Data
public class CreateGroupMessageRequest {
    @NotBlank(message = "Name is mandatory")
    @Length(min = 1, max = 50, message = "Name must be less than 50 characters")
    private String name;
    @NotEmpty(message = "Participant IDs are mandatory")
    private UUID[] participantIds;
    private CloudinaryMedia avatar;
}
