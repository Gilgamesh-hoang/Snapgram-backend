package org.snapgram.service.user;

import org.snapgram.dto.GooglePojo;
import org.snapgram.dto.request.ChangePasswordRequest;
import org.snapgram.dto.request.ProfileRequest;
import org.snapgram.dto.request.SignupRequest;
import org.snapgram.dto.response.UserDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Interface for User Service.
 * This interface provides methods for user management.
 */
public interface IUserService {

    UserDTO findByEmail(String email);

    UserDTO findById(UUID id);

    UserDTO findByNickname(String nickname);

    void deleteInactiveUsers(int days);

    /**
     * Deletes a user.
     *
     * @param userDTO the user to delete
     * @return the deleted user as a UserDTO
     */
    UserDTO deleteUser(UserDTO userDTO);

    /**
     * Creates a new user.
     *
     * @param request the signup request containing user details
     * @return the created user as a UserDTO
     */
    UserDTO createUser(SignupRequest request);

    UserDTO createUser(GooglePojo googlePojo);

    /**
     * Checks if an email already exists in the system.
     *
     * @param email the email to check
     * @return true if the email exists, false otherwise
     */
    boolean isEmailExists(String email);

    /**
     * Checks if a nickname already exists in the system.
     *
     * @param nickname the nickname to check
     * @return true if the nickname exists, false otherwise
     */
    boolean isNicknameExists(String nickname);

    /**
     * Verifies a user's email with a provided code.
     *
     * @param email the email to verify
     * @param code  the verification code
     * @return true if the verification is successful, false otherwise
     */
    boolean verifyEmail(String email, String code);

    /**
     * Generates a forgot password code for a user.
     *
     * @param email the email of the user who forgot their password
     * @return the generated forgot password code
     */
    String generateForgotPasswordCode(String email);


    List<UserDTO> findFriendsByUserId(UUID userId);

    List<UserDTO> findRandomUsers(int number, List<UUID> exceptIds);

    UserDTO editUserInfo(UUID id, ProfileRequest request, MultipartFile avatar);

    void changePassword(UUID id, ChangePasswordRequest request);
}