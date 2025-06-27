package org.snapgram.service.user;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.entity.database.user.User;
import org.snapgram.entity.elasticsearch.UserDocument;
import org.snapgram.exception.ResourceNotFoundException;
import org.snapgram.mapper.UserMapper;
import org.snapgram.repository.database.UserRepository;
import org.snapgram.repository.elasticsearch.user.UserElasticRepo;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
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
            log.warn("User document with ID {} already exists, skipping creation.", id);
            return;
        }else if(user == null) {
            log.error("User with ID {} not found in the database.", id);
            return;
        }
        userElasticRepo.save(userMapper.toUserDocument(user));
    }

    @Override
    public void updateUser(UUID id) {
        UserDocument userDocument = validAndGetUserDocument(id);
        User user = validAndGetUserRepo(id);
        if (userDocument == null) {
            log.error("User document with ID {} not found in Elasticsearch.", id);
            throw new ResourceNotFoundException("User document not found for ID: " + id);
        } else if (user == null) {
            log.error("User with ID {} not found in the database.", id);
            throw new ResourceNotFoundException("User not found for ID: " + id);
        }
        userMapper.updateUserDocumentFromUser(userDocument, user);
        userElasticRepo.save(userDocument);
    }

    @Override
    public void deleteUser(UUID id) {
        UserDocument userDocument = validAndGetUserDocument(id);
        if (userDocument == null) {
            log.error("User document with ID {} not found in Elasticsearch.", id);
            return;
        }
        userElasticRepo.delete(userDocument);
    }

    private User validAndGetUserRepo(UUID id) {
        return userRepository.findById(id).orElse(null);
    }

    private UserDocument validAndGetUserDocument(UUID id) {
        return userElasticRepo.findById(id).orElse(null);
    }
}
