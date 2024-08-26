package org.snapgram.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.snapgram.validation.password_matches.PasswordMatches;

import java.io.Serializable;

@Data
@PasswordMatches
public class ChangePasswordRequest implements Serializable {

    @NotBlank
    @Size(min = 8)
    private String currentPassword;

    @NotBlank
    @Size(min = 8)
    private String newPassword;

    @NotBlank
    @Size(min = 8)
    private String confirmNewPassword;

}
