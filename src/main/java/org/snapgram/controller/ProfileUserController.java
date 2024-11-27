package org.snapgram.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.request.ProfileRequest;
import org.snapgram.dto.response.ResponseObject;
import org.snapgram.dto.response.UserInfoDTO;
import org.snapgram.service.user.IProfileService;
import org.snapgram.util.AppConstant;
import org.snapgram.validation.media.ValidMedia;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("${API_PREFIX}/users")
@Validated
public class ProfileUserController {
    IProfileService profileService;
    ObjectMapper objectMapper;

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseObject<UserInfoDTO> updateProfile(
            @CookieValue(AppConstant.REFRESH_TOKEN) @NotBlank String refreshToken,
            @RequestBody @Valid ProfileRequest request){
        UserInfoDTO response = profileService.updateProfile(request, refreshToken);
        return new ResponseObject<>(HttpStatus.OK, "Profile updated successfully", response);
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseObject<UserInfoDTO> updateProfile(
            @CookieValue(AppConstant.REFRESH_TOKEN) @NotBlank String refreshToken,
            @RequestPart("profile") @Valid String profileJson,
            @RequestPart(value = "avatar", required = false) @ValidMedia MultipartFile avatar) throws JsonProcessingException {
        ProfileRequest request = objectMapper.readValue(profileJson, ProfileRequest.class);
        UserInfoDTO response = profileService.updateProfile(request, avatar, refreshToken);
        return new ResponseObject<>(HttpStatus.OK, "Profile updated successfully", response);
    }

}
