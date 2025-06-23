package org.snapgram.service.user;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.entity.database.user.User;
import org.snapgram.entity.elasticsearch.UserDocument;
import org.snapgram.exception.ResourceNotFoundException;
import org.snapgram.mapper.UserMapper;
import org.snapgram.repository.database.UserRepository;
import org.snapgram.repository.elasticsearch.user.UserElasticRepo;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserSyncDataService implements IUserSyncDataService{
    UserElasticRepo userElasticRepo;
    UserRepository userRepository;
    UserMapper userMapper;

    @Override
    public void createUser(UUID id) {
        User user = validAndGetUserRepo(id);
        UserDocument userDocument = validAndGetUserDocument(id);
        if(userDocument != null) {
            throw new ResourceNotFoundException("User document already exists");
        }
        userElasticRepo.save(userMapper.toUserDocument(user));
    }

    @Override
    public void updateUser(UUID id) {
        UserDocument userDocument = validAndGetUserDocument(id);
        User user = validAndGetUserRepo(id);
        userMapper.updateUserDocumentFromUser(userDocument, user);
        userElasticRepo.save(userDocument);
    }

    @Override
    public void deleteUser(UUID id) {
        userElasticRepo.delete(validAndGetUserDocument(id));
    }

    private User validAndGetUserRepo(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private UserDocument validAndGetUserDocument(UUID id) {
        return userElasticRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
