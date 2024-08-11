package org.snapgram.service.user;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.response.UserDTO;
import org.snapgram.entity.database.User;
import org.snapgram.mapper.UserMapper;
import org.snapgram.repository.database.IUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true)
class TransactionalUserService {
    private IUserRepository userRepository;
    private UserMapper userMapper;

    @Transactional
    public UserDTO deleteUserTransactional(User userEntity) {
        userEntity.setIsDeleted(true);
        userRepository.save(userEntity);
        return userMapper.toDTO(userEntity);
    }
}
