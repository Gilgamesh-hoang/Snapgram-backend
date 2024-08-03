package org.snapgram.service.user;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.response.UserDTO;
import org.snapgram.entity.User;
import org.snapgram.mapper.UserMapper;
import org.snapgram.repository.IUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true)
public class TransactionalUserService {
    private IUserRepository userRepository;
    private UserMapper userMapper;

    @Transactional
    protected UserDTO deleteUserTransactional(User userEntity) {
        userEntity.setIsDeleted(true);
        userRepository.save(userEntity);
        return userMapper.toDTO(userEntity);
    }
}
