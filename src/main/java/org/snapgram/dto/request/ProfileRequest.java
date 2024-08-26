package org.snapgram.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.snapgram.validation.enum_pattern.EnumPattern;
import org.snapgram.enums.Gender;

@Data
public class ProfileRequest {
    @NotBlank(message = "Nickname is mandatory")
    @Size(min = 3, max = 50, message = "NickName must be between 3 and 50 characters")
    private String nickname;
    @NotBlank(message = "Full Name is mandatory")
    @Size(max = 100, message = "Full Name must be less than 100 characters")
    private String fullName;
    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email should be valid")
    private String email;
    @NotNull(message = "Gender is mandatory")
    @EnumPattern(name = "gender", regexp = "FEMALE|MALE")
    private Gender gender;
    @Length(max = 400)
    private String bio;
}
