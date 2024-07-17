package org.snapgram.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.model.request.SignupRequest;
import org.snapgram.service.impl.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("${API_PREFIX}/users")
public class UsersController {
    UserService userService;

    @PostMapping
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequest request) {
        boolean created = userService.createUser(request);
        if (!created) {
            return new ResponseEntity<>("User creation failed", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("User created successfully", HttpStatus.OK);
    }
}
