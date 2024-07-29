package org.snapgram.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.snapgram.annotation.enum_pattern.EnumPattern;
import org.snapgram.annotation.password_matches.PasswordMatches;
import org.snapgram.enums.Gender;

import java.io.Serializable;

@Data
@PasswordMatches
public class SignupRequest implements Serializable {
    @NotBlank(message = "Nickname is mandatory")
    @Size(min = 3, max = 50, message = "NickName must be between 3 and 50 characters")
    private String nickname;

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is mandatory")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Confirm Password is mandatory")
    @Size(min = 8, message = "Confirm Password must be at least 8 characters")
    private String confirmPassword;

    @NotBlank(message = "Full Name is mandatory")
    @Size(max = 100, message = "Full Name must be less than 100 characters")
    private String fullName;

    @NotNull(message = "Gender is mandatory")
    @EnumPattern(name = "gender", regexp = "FEMALE|MALE")
    private Gender gender;
}
