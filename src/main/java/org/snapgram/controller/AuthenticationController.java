package org.snapgram.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.dto.request.AuthenticationRequest;
import org.snapgram.dto.request.TokenRequest;
import org.snapgram.dto.request.VerificationRequest;
import org.snapgram.dto.response.JwtResponse;
import org.snapgram.dto.response.ResponseObject;
import org.snapgram.dto.response.UserDTO;
import org.snapgram.kafka.producer.KeyPairProducer;
import org.snapgram.mapper.UserMapper;
import org.snapgram.service.authentication.IAuthenticationService;
import org.snapgram.service.user.IUserService;
import org.snapgram.util.AppConstant;
import org.snapgram.util.CookieUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("${API_PREFIX}/auth")
@Validated
@Slf4j
public class AuthenticationController {
    IUserService userService;
    KeyPairProducer keyPairProducer;
    IAuthenticationService authenticationService;
    ClientRegistrationRepository clientRegistrationRepository;
    UserMapper userMapper;

    @NonFinal
    @Value("${jwt.refresh_token.duration}")
    long REFRESH_TOKEN_LIFETIME;

    @PostMapping("/google")
    public ResponseObject<JwtResponse> google(@RequestBody @Valid TokenRequest request, HttpServletResponse response) {
        DefaultOAuth2UserService oAuth2Service = new DefaultOAuth2UserService();
        OAuth2UserRequest userRequest = new OAuth2UserRequest(clientRegistrationRepository.findByRegistrationId("google"),
                new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, request.getToken(), null, null, null));
        OAuth2User user = oAuth2Service.loadUser(userRequest);
        JwtResponse jwtObj = authenticationService.oauth2GoogleLogin(userMapper.toGooglePojo(user));
        return createLoginResponse(response, jwtObj);
    }

    private ResponseObject<JwtResponse> createLoginResponse(HttpServletResponse response, JwtResponse jwtObj) {
        // divide REFRESH_TOKEN_LIFETIME by 1000 to convert milliseconds to seconds and plus 100 seconds for safety
        final int TTL_SECOND = (int) (REFRESH_TOKEN_LIFETIME / 1000) + 100;
        // Set refresh token in HTTP-Only cookie
        Cookie refreshTokenCookie = CookieUtil.createCookie(AppConstant.REFRESH_TOKEN, jwtObj.getRefreshToken(),
                "localhost", TTL_SECOND, true, false);
        response.addCookie(refreshTokenCookie);
        return new ResponseObject<>(HttpStatus.OK, "Authentication successfully", jwtObj);
    }


    @PostMapping("/login")
    public ResponseObject<JwtResponse> authenticate(@RequestBody @Valid AuthenticationRequest request, HttpServletResponse response) {
        JwtResponse jwtObj = authenticationService.login(request);
        return createLoginResponse(response, jwtObj);
    }

    @PostMapping("/refresh-token")
    public ResponseObject<JwtResponse> refreshToken(@CookieValue(AppConstant.REFRESH_TOKEN) @NotBlank String refreshToken,
                                                    HttpServletResponse response
    ) {
        JwtResponse jwtObj = authenticationService.refreshToken(refreshToken);
        return createLoginResponse(response, jwtObj);
    }

    @PostMapping("/logout")
    public ResponseObject<Void> logout(@RequestHeader("Authorization") String authHeader,
                                       @CookieValue(AppConstant.REFRESH_TOKEN) @NotBlank String refreshToken,
                                       HttpServletResponse response
    ) {
        String jwtToken = authHeader.substring("Bearer ".length());
        authenticationService.logout(jwtToken, refreshToken);
        // clear refresh token cookie
        Cookie refreshTokenCookie = CookieUtil.createCookie(AppConstant.REFRESH_TOKEN, null, "localhost",
                0, true, false);
        response.addCookie(refreshTokenCookie);
        return new ResponseObject<>(HttpStatus.OK, "Logout successfully", null);
    }

    @PostMapping("/verification-email")
    public ResponseObject<Boolean> verifyEmail(@RequestBody @Valid VerificationRequest request) {
        boolean isVerified = userService.verifyEmail(request.getEmail(), request.getCode());
        if (!isVerified) {
            return new ResponseObject<>(HttpStatus.BAD_REQUEST, "Code is expired", false);
        }
        UserDTO user = userService.getByEmail(request.getEmail());
        keyPairProducer.sendGenerateKeyPair(user.getId());

        return new ResponseObject<>(HttpStatus.OK, "Email verified successfully", true);
    }
}
