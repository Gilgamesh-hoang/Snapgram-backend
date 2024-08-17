package org.snapgram.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.request.EmailRequest;
import org.snapgram.dto.request.SignupRequest;
import org.snapgram.dto.response.ResponseObject;
import org.snapgram.dto.response.UserDTO;
import org.snapgram.exception.ResourceNotFoundException;
import org.snapgram.service.mail.IEmailService;
import org.snapgram.service.redis.IRedisService;
import org.snapgram.service.suggestion.FriendSuggestionService;
import org.snapgram.service.user.IUserService;
import org.snapgram.util.RedisKeyUtil;
import org.snapgram.util.UserSecurityHepler;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("${API_PREFIX}/users")
@Validated
public class UserController {
    IRedisService redisService;
    IUserService userService;
    FriendSuggestionService friendSuggestionService;
    IEmailService emailService;

    @GetMapping
    public ResponseObject<UserDTO> getUser() {
        return new ResponseObject<>(HttpStatus.OK, UserDTO.builder().email("nwaeuibgfuiebf").build());
    }

    @GetMapping("/friend-suggestions")
    public ResponseObject<List<UserDTO>> friendSuggestion(
            @RequestParam(value = "pageNum", defaultValue = "1") @Min(0) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "15") @Min(0) Integer pageSize
    ) {
        String email = UserSecurityHepler.getCurrentUser().getUsername();
        UserDTO user = userService.findByEmail(email);

        // Calculate the start and end indices for pagination
        int start = (pageNumber - 1) * pageSize;
        int end = pageNumber * pageSize - 1;

        // Try to get the list of friend suggestions from Redis
        List<UserDTO> users = redisService.getList(RedisKeyUtil.getFriendSuggestKey(email), start, end);

        if (users == null ) {
            // Generate friend suggestions
            users = friendSuggestionService.recommendFriends(user.getId());

            List<UserDTO> finalUsers = new ArrayList<>(users);
            CompletableFuture.runAsync(() -> {
                redisService.saveList(RedisKeyUtil.getFriendSuggestKey(email), finalUsers);
                redisService.setTimeout(RedisKeyUtil.getFriendSuggestKey(email), 5, TimeUnit.DAYS);
            });

            // Get the sublist of users based on the pagination parameters
            users = users.subList(start, Math.min(users.size(), end + 1));
        }

        return new ResponseObject<>(HttpStatus.OK, users);
    }

    @GetMapping("/me")
    public ResponseObject<UserDTO> getCurrentUser() {
        String email = UserSecurityHepler.getCurrentUser().getUsername();
        return new ResponseObject<>(HttpStatus.OK, userService.findByEmail(email));
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
