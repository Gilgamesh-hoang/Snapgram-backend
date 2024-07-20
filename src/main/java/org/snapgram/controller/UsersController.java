package org.snapgram.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.model.request.SignupRequest;
import org.snapgram.model.response.ResponseObject;
import org.snapgram.service.impl.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseObject<Void> signup(@Valid @RequestBody SignupRequest request) {
        boolean created = userService.createUser(request);
        if (!created) {
            return new ResponseObject<>(HttpStatus.BAD_REQUEST, "User creation failed");
        }
        return new ResponseObject<>(HttpStatus.CREATED, "User created successfully");
    }
}
