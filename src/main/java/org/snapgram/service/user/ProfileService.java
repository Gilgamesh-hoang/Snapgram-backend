package org.snapgram.service.user;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.CustomUserSecurity;
import org.snapgram.dto.request.ProfileRequest;
import org.snapgram.dto.response.UserInfoDTO;
import org.snapgram.service.follow.IFollowService;
import org.snapgram.service.post.IPostService;
import org.snapgram.service.token.ITokenService;
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
    ITokenService tokenService;

    @Override
    public UserInfoDTO getProfile(String nickname) {
        UserInfoDTO profile = userService.getUserInfo(nickname);

        profile.setFollowerNumber(followService.countFollowers(profile.getId()));
        profile.setFolloweeNumber(followService.countFollowees(profile.getId()));

        profile.setPostNumber(postService.countByUser(profile.getId()));
        return profile;
    }

    @Override
    public UserInfoDTO updateProfile(ProfileRequest request, MultipartFile avatar, String refreshToken) {
        // Get the current logged-in user's details
        CustomUserSecurity userContext = UserSecurityHelper.getCurrentUser();

        // Check if the email provided in the request is different from the current user's email
        // //and if it already exists in the system
        if (!request.getEmail().equals(userContext.getEmail())) {
            if (userService.isEmailExists(request.getEmail())) {
                throw new IllegalArgumentException("Email already exists");
            } else {
                tokenService.blacklistAllUserTokens(userContext.getId(), refreshToken);
            }
        }

        // Check if the nickname provided in the request is different from the current user's nickname
        // and if it already exists in the system
        if (!request.getNickname().equals(userContext.getNickname()) && userService.isNicknameExists(request.getNickname())) {
            throw new IllegalArgumentException("Nickname already exists");
        }

        if (avatar != null) {
            String contentType = avatar.getContentType();

            // Check if the content type of the avatar file is not an image
            if (contentType == null || !contentType.startsWith("image")) {
                throw new IllegalArgumentException("Avatar must be an image");
            }
        }

        return userService.editUserInfo(userContext.getId(), request, avatar);
    }
}
