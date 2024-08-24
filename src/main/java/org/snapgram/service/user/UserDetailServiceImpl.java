package org.snapgram.service.user;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.CustomUserSecurity;
import org.snapgram.entity.database.User;
import org.snapgram.exception.UserNotFoundException;
import org.snapgram.repository.database.UserRepository;
import org.springframework.data.domain.Example;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService {

    UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Example<User> example = Example.of(User.builder().email(username).isDeleted(false).isActive(true).build());
        User user = userRepo.findOne(example).orElse(null);
        if (user == null) {
            throw new UserNotFoundException("User not found with email: " + username);
        }
        return new CustomUserSecurity(user);
    }

}
