package org.snapgram.service.user;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.response.ProfileDTO;
import org.snapgram.dto.response.UserDTO;
import org.snapgram.mapper.UserMapper;
import org.snapgram.service.follow.IFollowService;
import org.snapgram.service.post.IPostService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileService implements IProfileService {
    IUserService userService;
    IPostService postService;
    IFollowService followService;
    UserMapper userMapper;

    @Override
    public ProfileDTO getProfile(String nickname) {
        UserDTO user = userService.findByNickname(nickname);
        ProfileDTO profile = userMapper.toProfileDTO(user);

        profile.setFollowerNumber(followService.countFollowers(user.getId()));
        profile.setFolloweeNumber(followService.countFollowees(user.getId()));

        profile.setPostNumber(postService.countByUser(user.getId()));
        return profile;
    }
}
