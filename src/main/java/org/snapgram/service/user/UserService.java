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
    public UserDTO deleteUser(UserDTO user) {
        if (user == null)
            return null;

        User userEntity = null;
        if (user.getId() != null) {
            userEntity = userRepository.findById(user.getId()).orElseThrow(()
                    -> new ResourceNotFoundException("User not found"));
        } else if (user.getEmail() != null) {
            userEntity = userRepository.findByEmail(user.getEmail()).orElseThrow(()
                    -> new ResourceNotFoundException("User not found"));
        } else if (user.getNickname() != null) {
            userEntity = userRepository.findByNickname(user.getNickname()).orElseThrow(()
                    -> new ResourceNotFoundException("User not found"));
        }

        if (userEntity != null) {
            userEntity.setIsDeleted(true);
            userRepository.save(userEntity);
            return userMapper.toDTO(userEntity);
        } else {
            return null;
        }
    }

    @Override
    public UserDTO createUser(SignupRequest request) {
        if (isEmailExists(request.getEmail())) {
            throw new IllegalArgumentException("User with email already exists");
        }
        if (isNicknameExists(request.getNickname())) {
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
    public boolean isEmailExists(String email) {
        // Find the user in the database with the provided email
        Example<User> example = Example.of(User.builder().email(email).isDeleted(false).isActive(false).build());
        User user = userRepository.findOne(example).orElse(null);

        // If no user is found, return false
        if (user == null)
            return false;

        if (isVerificationExpired(3, user.getCreatedAt())) {
            deleteUser(UserDTO.builder().email(email).build());
            return false;
        }

        // If the user is active or the user's creation time plus 3 days is not before the current time, return true
        return true;
    }

    @Override
    public boolean isNicknameExists(String nickname) {
        // Find the user in the database with the provided nickname
        Example<User> example = Example.of(User.builder().nickname(nickname).isDeleted(false).isActive(false).build());
        User user = userRepository.findOne(example).orElse(null);

        // If no user is found, return false
        if (user == null)
            return false;

        if (isVerificationExpired(3, user.getCreatedAt())) {
            deleteUser(UserDTO.builder().nickname(nickname).build());
            return false;
        }
        // If the user is active or the user's creation time plus 3 days is not before the current time, return true
        return true;
    }

    private boolean isVerificationExpired(int numDays, Timestamp timestamp) {
        LocalDateTime userCreationTime = timestamp.toLocalDateTime();
        LocalDateTime currentTime = new Timestamp(System.currentTimeMillis()).toLocalDateTime();

        // If the user's creation time plus days is before the current time, return false
        // it means the user's email verification has expired
        return userCreationTime.plusDays(numDays).isBefore(currentTime);
    }

    @Override
    public boolean verifyEmail(String email, String code) {
        Example<User> example = Example.of(User.builder().email(email).isDeleted(false).activeCode(code).build());
        User user = userRepository.findOne(example).orElse(null);

        if (user == null)
            throw new ResourceNotFoundException("Email or code is invalid");

        if (isVerificationExpired(3, user.getCreatedAt())) {
            return false;
        }

        // If the user's creation time plus 3 days is not before the current time, set the user's active code to null and set the user as active
        user.setActiveCode(null);
        user.setIsActive(true);

        // Save the updated user to the database
        userRepository.save(user);

        // Return true indicating the email verification was successful
        return true;
    }

    @Override
    public String generateForgotPasswordCode(String email) {
        Example<User> example = Example.of(User.builder().email(email).isActive(true).isDeleted(false).build());
        User user = userRepository.findOne(example).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String newPassword = Generators.randomBasedGenerator().generate().toString()
                .replace("-", "").substring(0, 10);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return newPassword;
    }
}
