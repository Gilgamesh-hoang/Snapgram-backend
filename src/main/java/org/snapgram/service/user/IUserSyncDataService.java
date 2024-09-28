package org.snapgram.service.user;

import java.util.UUID;

public interface IUserSyncDataService {
    void createUser(UUID id);

    void updateUser(UUID id);

    void deleteUser(UUID id);
}
