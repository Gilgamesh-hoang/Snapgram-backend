package org.snapgram.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.entity.User;
import org.snapgram.enums.Gender;
import org.snapgram.mapper.UserMapper;
import org.snapgram.model.request.SignupRequest;
import org.snapgram.repository.UserRepository;
import org.snapgram.service.IUserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService implements IUserService {
    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    @Override
    public boolean createUser(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("User with email already exists");
        }
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new IllegalArgumentException("User with nickname already exists");
        }

        // Map the SignupRequest to a User entity
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        // Save the User entity to the database
        user = userRepository.save(user);

        // Return true if the user was created successfully, false otherwise
        return user != null;
    }

    @Override
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean nicknameExists(String nickname) {
        return userRepository.existsByNickname(nickname);
    }
}
