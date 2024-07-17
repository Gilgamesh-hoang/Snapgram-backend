package org.snapgram.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationRequest {
    @NotBlank(message = "PARAMETER_MISSING")
    String username;
    @NotBlank(message = "PARAMETER_MISSING")
    String password;
}
