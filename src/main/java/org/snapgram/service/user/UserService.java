package org.snapgram.service.user;

import com.fasterxml.uuid.Generators;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.entity.User;
import org.snapgram.exception.ResourceNotFoundException;
import org.snapgram.mapper.UserMapper;
import org.snapgram.model.request.SignupRequest;
import org.snapgram.model.response.UserDTO;
import org.snapgram.repository.UserRepository;
import org.springframework.data.domain.Example;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService implements IUserService {
    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    @Override
    public UserDTO createUser(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("User with email already exists");
        }
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new IllegalArgumentException("User with nickname already exists");
        }

        // Map the SignupRequest to a User entity
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActiveCode(Generators.randomBasedGenerator().generate().toString());
        // Save the User entity to the database
        user = userRepository.save(user);

        // Return true if the user was created successfully, false otherwise
        return userMapper.toDTO(user);
    }

    @Override
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean nicknameExists(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    @Override
    public boolean verifyEmail(String email, String code) {
        User user = userRepository.findByEmailAndActiveCode(email, code).orElse(null);
        if (user == null)
            throw new ResourceNotFoundException("Email or code is invalid");

        LocalDateTime userCreationTime = user.getCreatedAt().toLocalDateTime();
        LocalDateTime currentTime = new Timestamp(System.currentTimeMillis()).toLocalDateTime();
        if (userCreationTime.plusDays(3).isBefore(currentTime)) {
            return false;
        }

        user.setActiveCode(null);
        user.setIsActive(true);
        userRepository.save(user);
        return true;
    }

    @Override
    public String generateForgotPasswordCode(String email) {
        Example<User> example = Example.of(User.builder().email(email).isActive(true).build());
        User user = userRepository.findOne(example).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String newPassword = Generators.randomBasedGenerator().generate().toString()
                .replaceAll("-", "").substring(0, 10);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return newPassword;
    }
}
