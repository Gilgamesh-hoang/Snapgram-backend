package org.snapgram.service.user;

import org.snapgram.dto.request.ProfileRequest;
import org.snapgram.dto.response.UserInfoDTO;
import org.springframework.web.multipart.MultipartFile;

public interface IProfileService {
    UserInfoDTO getProfile(String nickname);

    UserInfoDTO updateProfile(ProfileRequest request, MultipartFile avatar, String refreshToken);
}

