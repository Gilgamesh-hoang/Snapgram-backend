package org.snapgram.service.user;

import com.fasterxml.uuid.Generators;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.dto.GooglePojo;
import org.snapgram.dto.request.ChangePasswordRequest;
import org.snapgram.dto.request.ProfileRequest;
import org.snapgram.dto.request.SignupRequest;
import org.snapgram.dto.response.UserDTO;
import org.snapgram.entity.database.User;
import org.snapgram.entity.elasticsearch.UserDocument;
import org.snapgram.enums.Gender;
import org.snapgram.exception.ResourceNotFoundException;
import org.snapgram.exception.UploadFileException;
import org.snapgram.exception.UserNotFoundException;
import org.snapgram.mapper.UserMapper;
import org.snapgram.repository.database.UserRepository;
import org.snapgram.repository.elasticsearch.user.ICustomUserElasticRepo;
import org.snapgram.service.upload.MediaUploader;
import org.springframework.data.domain.Example;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService implements IUserService {
    ICustomUserElasticRepo userElastic;
    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    TransactionalUserService transactionalUserService;
    MediaUploader uploader;

    @Override
    public List<UserDTO> findRandomUsers(int number, List<UUID> exceptIds) {
        List<UUID> ids = userElastic.findRandomUsers(number, exceptIds).stream().map(UserDocument::getId).toList();
        return userRepository.findAllById(ids).stream().map(user -> {
            user.setBio(null);
            return userMapper.toDTO(user);
        }).toList();
    }

    @Override
    public UserDTO editUserInfo(UUID id, ProfileRequest request, MultipartFile avatar) {
        User user = findUserEntityById(id);
        if (user == null)
            throw new UserNotFoundException("User not found with id: " + id);

        userMapper.updateUserFromProfile(request, user);

        if (avatar != null) {
            String url = null;
            try {
                url = uploader.uploadFile(avatar);
            } catch (IOException e) {
                throw new UploadFileException("Failed to upload file: " + avatar.getOriginalFilename(), e);
            }
            user.setAvatarUrl(url);
        }

        userRepository.save(user);
        return userMapper.toDTO(user);
    }

    @Override
    public void changePassword(UUID id, ChangePasswordRequest request) {
        User user = findUserEntityById(id);
        if (user == null)
            throw new UserNotFoundException("User not found with id: " + id);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }


    @Override
    public UserDTO findByEmail(String email) {
        User user = findUserEntityByEmail(email);
        if (user == null)
            return null;
        user.setBio(null);
        return userMapper.toDTO(user);
    }

    private User findUserEntityByEmail(String email) {
        Example<User> example = Example.of(User.builder().email(email).isDeleted(false).build());
        return userRepository.findOne(example).orElse(null);
    }

    @Override
    public UserDTO findById(UUID id) {
        User user = findUserEntityById(id);
        if (user == null)
            return null;
        user.setBio(null);
        return userMapper.toDTO(user);
    }

    private User findUserEntityById(UUID id) {
        Example<User> example = Example.of(User.builder().id(id).isDeleted(false).isActive(true).build());
        return userRepository.findOne(example).orElse(null);
    }

    @Override
    public UserDTO findByNickname(String nickname) {
        return userMapper.toDTO(findUserEntityByNickname(nickname));
    }

    @Override
    @Transactional
    public void deleteInactiveUsers(int days) {
        Example<User> example = Example.of(User.builder().isDeleted(false).isActive(false).build());
        List<User> inactiveUsers = new ArrayList<>();
        userRepository.findAll(example).forEach(user -> {
            if (isVerificationExpired(days, user.getCreatedAt())) {
                user.setIsDeleted(true);
                inactiveUsers.add(user);
            }
        });
        userRepository.saveAll(inactiveUsers);
    }

    private boolean isVerificationExpired(int numDays, Timestamp timestamp) {
        LocalDateTime userCreationTime = timestamp.toLocalDateTime();
        LocalDateTime currentTime = new Timestamp(System.currentTimeMillis()).toLocalDateTime();

        // If the user's creation time plus days is before the current time, return false
        // it means the user's email verification has expired
        return userCreationTime.plusDays(numDays).isBefore(currentTime);
    }

    private User findUserEntityByNickname(String nickname) {
        Example<User> example = Example.of(User.builder().nickname(nickname).isActive(true).isDeleted(false).build());
        return userRepository.findOne(example).orElseThrow(()
                -> new UserNotFoundException("User not found with nickname: " + nickname)
        );
    }

    @Override
    public UserDTO deleteUser(UserDTO user) {
        if (user == null)
            return null;

        User userEntity = null;
        if (user.getId() != null) {
            userEntity = findUserEntityById(user.getId());
        } else if (user.getEmail() != null) {
            userEntity = findUserEntityByEmail(user.getEmail());
        } else if (user.getNickname() != null) {
            userEntity = findUserEntityByNickname(user.getNickname());
        }

        if (userEntity != null) {
            return transactionalUserService.deleteUserTransactional(userEntity);
        } else {
            return null;
        }
    }

    @Override
    @Transactional
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
        user.setIsActive(false);
        user.setIsDeleted(false);
        // Save the User entity to the database
        user = userRepository.save(user);

        // Return true if the user was created successfully, false otherwise
        return userMapper.toDTO(user);
    }

    @Override
    @Transactional
    public UserDTO createUser(GooglePojo googlePojo) {
        if (isEmailExists(googlePojo.getEmail())) {
            throw new IllegalArgumentException("User with email already exists");
        }

        String generateNickname = "user_" + googlePojo.getSub().substring(0, 8);

        User user = userMapper.toEntity(googlePojo);
        user.setNickname(generateNickname);
        user.setPassword(passwordEncoder.encode(Generators.randomBasedGenerator().toString()));
        user.setIsActive(true);
        user.setIsDeleted(false);
        user.setGender(Gender.MALE);
        // Save the User entity to the database
        user = userRepository.save(user);

        // Return true if the user was created successfully, false otherwise
        return userMapper.toDTO(user);
    }

    @Override
    public boolean isEmailExists(String email) {
        // Find the user in the database with the provided email
        Example<User> example = Example.of(User.builder().email(email).isActive(true).isDeleted(false).build());
        User user = userRepository.findOne(example).orElse(null);

        // If no user is found, return false
        return user != null;
    }

    @Override
    public boolean isNicknameExists(String nickname) {
        // Find the user in the database with the provided nickname
        Example<User> example = Example.of(User.builder().nickname(nickname).isActive(true).isDeleted(false).build());
        User user = userRepository.findOne(example).orElse(null);

        // If no user is found, return false
        return user != null;
    }


    @Override
    @Transactional
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
    @Transactional
    public String generateForgotPasswordCode(String email) {
        Example<User> example = Example.of(User.builder().email(email).isActive(true).isDeleted(false).build());
        User user = userRepository.findOne(example).orElseThrow(UserNotFoundException::new);
        String newPassword = Generators.randomBasedGenerator().generate().toString()
                .replace("-", "").substring(0, 10);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return newPassword;
    }

    @Override
    public List<UserDTO> findFriendsByUserId(UUID userId) {
        return userRepository.findFollowers(userId).stream().map(userMapper::toDTO).toList();
    }


}
