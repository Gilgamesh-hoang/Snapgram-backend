package org.snapgram.service;

import org.snapgram.model.request.SignupRequest;

public interface IUserService {
    boolean createUser(SignupRequest request);

    boolean emailExists(String email);

    boolean nicknameExists(String nickname);
}
