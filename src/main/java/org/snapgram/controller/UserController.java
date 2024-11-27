package org.snapgram.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.CustomUserSecurity;
import org.snapgram.dto.KeyPair;
import org.snapgram.dto.request.ChangePasswordRequest;
import org.snapgram.dto.request.EmailRequest;
import org.snapgram.dto.request.SignupRequest;
import org.snapgram.dto.response.JwtResponse;
import org.snapgram.dto.response.ResponseObject;
import org.snapgram.dto.response.UserDTO;
import org.snapgram.dto.response.UserInfoDTO;
import org.snapgram.exception.ResourceNotFoundException;
import org.snapgram.kafka.producer.KeyPairProducer;
import org.snapgram.kafka.producer.MailProducer;
import org.snapgram.kafka.producer.RedisProducer;
import org.snapgram.service.jwt.JwtService;
import org.snapgram.service.key.IKeyService;
import org.snapgram.service.redis.IRedisService;
import org.snapgram.service.suggestion.FriendSuggestionService;
import org.snapgram.service.token.ITokenService;
import org.snapgram.service.user.IProfileService;
import org.snapgram.service.user.IUserService;
import org.snapgram.util.AppConstant;
import org.snapgram.util.CookieUtil;
import org.snapgram.util.RedisKeyUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("${API_PREFIX}/users")
@Validated
public class UserController {
    MailProducer mailProducer;
    IRedisService redisService;
    IUserService userService;
    FriendSuggestionService friendSuggestionService;
    IProfileService profileService;
    ITokenService tokenService;
    JwtService jwtService;
    IKeyService keyService;
    KeyPairProducer keyPairProducer;
    RedisProducer redisProducer;

    @PostMapping("/change-password")
    public ResponseObject<JwtResponse> changePass(
            @RequestBody @Valid ChangePasswordRequest request,
            @RequestHeader("Authorization") String authHeader,
            @AuthenticationPrincipal CustomUserSecurity user,
            HttpServletResponse response
    ) throws ExecutionException, InterruptedException {
        // Call the service to change the password of the user
        userService.changePassword(user.getId(), request);

        // Extract the JWT token from the Authorization header by removing the "Bearer " prefix
        String jwtToken = authHeader.substring("Bearer ".length());

        // Add the JWT token to the blacklist, meaning it can no longer be used
        tokenService.blacklistAccessToken(jwtToken);

        // Add all refresh tokens associated with the user to the blacklist
        tokenService.blacklistAllUserTokens(user.getId());

        // Generate a new JWT access token for the user
        KeyPair keyPair = keyService.generateKeyPair();
        CompletableFuture<String> accessTokenFuture = jwtService.generateAccessToken(user.getUsername(), keyPair.getPrivateKeyAT());
        CompletableFuture<String> refreshTokenFuture = jwtService.generateRefreshToken(user.getUsername(), keyPair.getPrivateKeyRT());
        CompletableFuture.allOf(accessTokenFuture, refreshTokenFuture).join();
        // Save the new refresh token in the database, associated with the user
        keyPairProducer.sendGenerateKeyPair(user.getId(), keyPair);
        String refreshToken = refreshTokenFuture.get();
        tokenService.storeRefreshToken(refreshToken, user.getId());

        // Create a new cookie for the refresh token
        Cookie cookie = CookieUtil.createCookie(AppConstant.REFRESH_TOKEN, refreshToken,
                "localhost", 604800, true, false);
        response.addCookie(cookie);

        return new ResponseObject<>(HttpStatus.CREATED, "Set new password successfully",
                JwtResponse.builder().token(accessTokenFuture.get()).build());
    }

    @GetMapping
    public ResponseObject<UserInfoDTO> getUserInfo(@RequestParam("nickname") @NotBlank String nickname) {
        UserInfoDTO profile = profileService.getProfile(nickname);
        return new ResponseObject<>(HttpStatus.OK, profile);
    }


    @GetMapping("/friend-suggestions")
    public ResponseObject<List<UserDTO>> friendSuggestion(
            @AuthenticationPrincipal CustomUserSecurity user,
            @RequestParam(value = "pageNum", defaultValue = "1") @Min(0) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "15") @Min(0) @Max(50) Integer pageSize
    ) {
        // Calculate the start and end indices for pagination
        int start = (pageNumber - 1) * pageSize;
        int end = pageNumber * pageSize - 1;

        String redisKey = RedisKeyUtil.getFriendSuggestKey(user.getId());

        // Try to get the list of friend suggestions from Redis
        List<UserDTO> users = redisService.getList(redisKey, start, end);
        if (users != null) {
            return new ResponseObject<>(HttpStatus.OK, users);
        }

        // Generate friend suggestions
        users = friendSuggestionService.recommendFriends(user.getId());
        redisProducer.sendSaveList(redisKey, users, 5, TimeUnit.DAYS);

        // Get the sublist of users based on the pagination parameters
        users = users.subList(start, Math.min(users.size(), end + 1));
        return new ResponseObject<>(HttpStatus.OK, users);
    }

    @GetMapping("/me")
    public ResponseObject<UserDTO> getCurrentUser(@AuthenticationPrincipal CustomUserSecurity user) {
        return new ResponseObject<>(HttpStatus.OK, userService.findByEmail(user.getEmail()));
    }


    @PostMapping("/forgot-password")
    public ResponseObject<Void> forgotPass(@RequestBody @Valid EmailRequest request) {
        boolean isExists = userService.isEmailExists(request.getEmail());
        if (!isExists) {
            throw new ResourceNotFoundException("Email not found");
        }
        String newPassword = userService.generateForgotPasswordCode(request.getEmail());
        mailProducer.sendForgotPasswordEmail(request.getEmail(), newPassword);
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
        UserInfoDTO user = userService.createUser(request);
        if (user == null) {
            return new ResponseObject<>(HttpStatus.BAD_REQUEST, "User creation failed");
        }
        mailProducer.sendVerificationEmail(user);
        return new ResponseObject<>(HttpStatus.CREATED, "User created successfully");
    }
}
