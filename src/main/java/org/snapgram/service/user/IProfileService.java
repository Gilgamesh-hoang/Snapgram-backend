package org.snapgram.service.user;

import org.snapgram.dto.request.ProfileRequest;
import org.snapgram.dto.response.ProfileDTO;
import org.springframework.web.multipart.MultipartFile;

public interface IProfileService {
    ProfileDTO getProfile(String nickname);

    ProfileDTO updateProfile(ProfileRequest request, MultipartFile avatar);
}

