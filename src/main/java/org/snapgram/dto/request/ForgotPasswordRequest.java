package org.snapgram.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ForgotPasswordRequest {
    private String email;
    private String newPassword;
}
