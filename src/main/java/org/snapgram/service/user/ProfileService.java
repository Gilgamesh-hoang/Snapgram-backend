package org.snapgram.service.user;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.CustomUserSecurity;
import org.snapgram.dto.request.ProfileRequest;
import org.snapgram.dto.response.ProfileDTO;
import org.snapgram.dto.response.UserDTO;
import org.snapgram.mapper.UserMapper;
import org.snapgram.service.follow.IFollowService;
import org.snapgram.service.post.IPostService;
import org.snapgram.util.UserSecurityHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    @Override
    public ProfileDTO updateProfile(ProfileRequest request, MultipartFile avatar) {
        // Get the current logged-in user's details
        CustomUserSecurity userContext = UserSecurityHelper.getCurrentUser();

        // Check if the email provided in the request is different from the current user's email
        // //and if it already exists in the system
        if (!request.getEmail().equals(userContext.getEmail()) && userService.isEmailExists(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Check if the nickname provided in the request is different from the current user's nickname
        // and if it already exists in the system
        if (!request.getNickname().equals(userContext.getNickname()) && userService.isNicknameExists(request.getNickname())) {
            throw new IllegalArgumentException("Nickname already exists");
        }

        String contentType = avatar.getContentType();

        // Check if the content type of the avatar file is not an image
        if (contentType == null || !contentType.startsWith("image")) {
            throw new IllegalArgumentException("Avatar must be an image");
        }

        UserDTO user = userService.editUserInfo(userContext.getId(), request, avatar);

        ProfileDTO profile = userMapper.toProfileDTO(user);
        profile.setBio(request.getBio());
        return profile;
    }
}
