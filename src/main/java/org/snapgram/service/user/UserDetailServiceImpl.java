package org.snapgram.service.user;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.exception.UserNotFoundException;
import org.snapgram.repository.IUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService {

    IUserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        org.snapgram.entity.User user = userRepo.findByEmail(username).orElse(null);
        if (user == null) {
            throw new UserNotFoundException("User not found with email: " + username);
        }
        return new User(user.getEmail(), user.getPassword(), new ArrayList<>());
    }

}
