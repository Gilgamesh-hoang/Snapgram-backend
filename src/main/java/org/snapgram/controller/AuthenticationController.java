package org.snapgram.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.dto.request.AuthenticationRequest;
import org.snapgram.dto.request.LogoutRequest;
import org.snapgram.dto.request.VerificationRequest;
import org.snapgram.dto.response.JwtResponse;
import org.snapgram.dto.response.ResponseObject;
import org.snapgram.service.authentication.IAuthenticationService;
import org.snapgram.service.user.IUserService;
import org.snapgram.util.CookieUtil;
import org.snapgram.util.SystemConstant;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("${API_PREFIX}/auth")
@Validated
@Slf4j
public class AuthenticationController {
    Environment env;
    IUserService userService;
    IAuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseObject<JwtResponse> authenticate(@RequestBody @Valid AuthenticationRequest request, HttpServletResponse response) {
        JwtResponse jwtObj = authenticationService.login(request);

        // Set refresh token in HTTP-Only cookie
        Cookie refreshTokenCookie = CookieUtil.createCookie(SystemConstant.REFRESH_TOKEN, jwtObj.getRefreshToken(),
                "localhost",  604800 , true, false);
        response.addCookie(refreshTokenCookie);

        return new ResponseObject<>(HttpStatus.OK, "Login successfully", jwtObj);
    }

    @PostMapping("/refresh-token")
    public ResponseObject<JwtResponse> refreshToken(@CookieValue(SystemConstant.REFRESH_TOKEN) @NotBlank String refreshToken) {
        JwtResponse jwtObj = authenticationService.refreshToken(refreshToken);
        return new ResponseObject<>(HttpStatus.OK, "Refresh token successfully", jwtObj);
    }

    @PostMapping("/logout")
    public ResponseObject<Void> logout(@RequestBody @Valid LogoutRequest request,
                                       @CookieValue(SystemConstant.REFRESH_TOKEN) @NotBlank String refreshToken,
                                       HttpServletResponse response
    ) {
        authenticationService.logout(request.getToken(), refreshToken);
        // clear refresh token cookie
        Cookie refreshTokenCookie = CookieUtil.createCookie(SystemConstant.REFRESH_TOKEN, null, "localhost",
                0, true, false);
        response.addCookie(refreshTokenCookie);
        return new ResponseObject<>(HttpStatus.OK, "Logout successfully", null);
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
