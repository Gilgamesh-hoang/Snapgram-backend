package org.snapgram.controller;

import org.snapgram.entity.User;
import org.snapgram.mapper.UserMapper;
import org.snapgram.model.UserDTO;
import org.snapgram.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${API_PREFIX}/users")
public class UsersController {
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserMapper userMapper;

    @GetMapping
    public UserDTO statusCheck() {
        // create a user and fake data for user

        User user = new User();
        user.setNickName("test");
        user.setEmail("a");
        user.setPassword("a");
        user.setFullName("a");
        user.setAvatarUrl("a");
        user.setBio("a");
        user.setGender(User.Gender.FEMALE);
        return userMapper.toDTO(userRepository.save(user));
    }
}
