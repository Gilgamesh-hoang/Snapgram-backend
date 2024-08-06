package org.snapgram.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.exception.ResourceNotFoundException;
import org.snapgram.dto.request.EmailRequest;
import org.snapgram.dto.request.SignupRequest;
import org.snapgram.dto.response.ResponseObject;
import org.snapgram.dto.response.UserDTO;
import org.snapgram.service.mail.IEmailService;
import org.snapgram.service.user.IUserService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("${API_PREFIX}/users")
@Validated
public class UserController {
    IUserService userService;
    IEmailService emailService;

    @GetMapping
    public ResponseObject<UserDTO> getUser() {
        return new ResponseObject<>(HttpStatus.OK, UserDTO.builder().email("nwaeuibgfuiebf").build());
    }

    @PostMapping("/forgot-password")
    public ResponseObject<Void> forgotPass(@RequestBody @Valid EmailRequest request) {
        boolean isExists = userService.isEmailExists(request.getEmail());
        if (!isExists) {
            throw new ResourceNotFoundException("Email not found");
        }
        String newPassword = userService.generateForgotPasswordCode(request.getEmail());
        emailService.sendForgotPasswordEmail(request.getEmail(), newPassword);
        return new ResponseObject<>(HttpStatus.CREATED, "Set new password successfully");
    }

    @GetMapping("/email-exists")
    public ResponseObject<Boolean> emailExists(@RequestParam @NotBlank @Email String email) {
        boolean exists = userService.isEmailExists(email);
        return new ResponseObject<>(HttpStatus.OK, exists);
    }

    @GetMapping("/nickname-exists")
    public ResponseObject<Boolean> nicknameExists(@RequestParam @NotBlank @Size(min = 2, max = 50) String nickname) {
        boolean exists = userService.isNicknameExists(nickname);
        return new ResponseObject<>(HttpStatus.OK, exists);
    }

    @PostMapping("/signup")
    public ResponseObject<Void> signup(@Valid @RequestBody SignupRequest request) {
        UserDTO user = userService.createUser(request);
        if (user == null) {
            return new ResponseObject<>(HttpStatus.BAD_REQUEST, "User creation failed");
        }
        emailService.sendVerificationEmail(user);
        return new ResponseObject<>(HttpStatus.CREATED, "User created successfully");
    }
}
