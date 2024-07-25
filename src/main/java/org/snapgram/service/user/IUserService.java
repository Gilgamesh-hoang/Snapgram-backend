package org.snapgram.service.user;

import org.snapgram.model.request.SignupRequest;
import org.snapgram.model.response.UserDTO;

public interface IUserService {
    UserDTO createUser(SignupRequest request);

    boolean emailExists(String email);

    boolean nicknameExists(String nickname);

    boolean verifyEmail(String email, String code);
}
