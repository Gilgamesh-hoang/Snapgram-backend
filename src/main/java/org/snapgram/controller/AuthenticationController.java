package org.snapgram.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snapgram.dto.request.AuthenticationRequest;
import org.snapgram.dto.request.LogoutRequest;
import org.snapgram.dto.request.VerificationRequest;
import org.snapgram.dto.response.JwtResponse;
import org.snapgram.dto.response.ResponseObject;
import org.snapgram.service.authentication.IAuthenticationService;
import org.snapgram.service.user.IUserService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("${API_PREFIX}/auth")
@Validated
public class AuthenticationController {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationController.class);
    IUserService userService;
    IAuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseObject<JwtResponse> authenticate(@RequestBody @Valid AuthenticationRequest request) {
        JwtResponse jwtObj = authenticationService.login(request);
        return new ResponseObject<>(HttpStatus.OK, "Login successfully", jwtObj);
    }

    @PostMapping("/logout")
    public ResponseObject<Void> logout(@RequestBody @Valid LogoutRequest request){
        authenticationService.logout(request);
        return  new ResponseObject<>(HttpStatus.OK, "Logout successfully", null);
    }

    @PostMapping("/verification-email")
    public ResponseObject<Boolean> verifyEmail(@RequestBody @Valid VerificationRequest request) {
        boolean isVerified = userService.verifyEmail(request.getEmail(), request.getCode());
        if (isVerified) {
            return new ResponseObject<>(HttpStatus.OK, "Email verified successfully", isVerified);
        }
        return new ResponseObject<>(HttpStatus.OK, "Code is expired", isVerified);
    }
}
