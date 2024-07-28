package org.snapgram.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.model.request.VerificationRequest;
import org.snapgram.model.response.ResponseObject;
import org.snapgram.service.user.IUserService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("${API_PREFIX}/auth")
@Validated
public class AuthenticationController {
    IUserService userService;
    @PostMapping("/verification-email")
    public ResponseObject<Boolean> verifyEmail(@RequestBody @Valid VerificationRequest request) {
        boolean isVerified = userService.verifyEmail(request.getEmail(), request.getCode());
        if (isVerified) {
            return new ResponseObject<>(HttpStatus.OK, "Email verified successfully", isVerified);
        }
        return new ResponseObject<>(HttpStatus.OK, "Code is expired", isVerified);
    }
}
