package org.snapgram.service.user;

import org.snapgram.dto.response.ProfileDTO;

public interface IProfileService {
    ProfileDTO getProfile(String nickname);
}

