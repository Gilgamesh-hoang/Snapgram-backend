package org.snapgram.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.model.request.SignupRequest;
import org.snapgram.model.response.ResponseObject;
import org.snapgram.model.response.UserDTO;
import org.snapgram.service.impl.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("${API_PREFIX}/users")
public class UsersController {
    UserService userService;

    @GetMapping
    public ResponseObject<UserDTO> getUsers() {
        return new ResponseObject<UserDTO>(HttpStatus.OK, "Users retrieved successfully",
                UserDTO.builder().email("cacafc").id(UUID.randomUUID()).nickName("fsfsgnuin").build());
    }

    @PostMapping
    public ResponseObject<Void> signup(@Valid @RequestBody SignupRequest request) {
        boolean created = userService.createUser(request);
        if (!created) {
            return new ResponseObject<>(HttpStatus.BAD_REQUEST, "User creation failed");
        }
        return new ResponseObject<>(HttpStatus.CREATED, "User created successfully");
    }
}
