package org.snapgram.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.snapgram.validation.password_matches.PasswordMatches;

import java.io.Serializable;

@Data
@PasswordMatches
public class ChangePasswordRequest implements Serializable {

    @NotBlank(message = "Current password is mandatory")
    @Size(min = 8)
    private String currentPassword;

    @NotBlank(message = "New password is mandatory")
    @Size(min = 8)
    private String newPassword;

    @NotBlank(message = "Confirm new password is mandatory")
    @Size(min = 8)
    private String confirmNewPassword;

}
