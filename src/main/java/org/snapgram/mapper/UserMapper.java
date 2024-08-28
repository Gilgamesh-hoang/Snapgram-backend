package org.snapgram.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.snapgram.dto.GooglePojo;
import org.snapgram.dto.request.ProfileRequest;
import org.snapgram.dto.request.SignupRequest;
import org.snapgram.dto.response.CreatorDTO;
import org.snapgram.dto.response.ProfileDTO;
import org.snapgram.dto.response.UserDTO;
import org.snapgram.entity.database.User;
import org.snapgram.entity.elasticsearch.UserDocument;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;

@Mapper(componentModel = "spring")
public interface UserMapper {

    ProfileDTO toProfileDTO(UserDTO user);
    UserDTO toDTO(User user);
    UserDTO toDTO(UserDocument user);
    Collection<UserDTO> toDTOs(Collection<UserDocument> users);

    @Mapping(source = "name", target = "fullName")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "picture", target = "avatarUrl")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "nickname", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "bio", ignore = true)
    @Mapping(target = "gender", ignore = true)
    @Mapping(target = "activeCode", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toEntity(GooglePojo googlePojo);


    default String map(Object value) {
        return value != null ? value.toString() : null;
    }

    @Mapping(source = "attributes.sub", target = "sub")
    @Mapping(source = "attributes.name", target = "name")
    @Mapping(source = "attributes.given_name", target = "givenName")
    @Mapping(source = "attributes.family_name", target = "familyName")
    @Mapping(source = "attributes.email", target = "email")
    @Mapping(source = "attributes.picture", target = "picture")
    @Mapping(source = "attributes.hd", target = "hd")
    @Mapping(source = "attributes.email_verified", target = "emailVerified")
    GooglePojo toGooglePojo(OAuth2User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "bio", ignore = true)
    @Mapping(target = "activeCode", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toEntity(SignupRequest request);

    @Mapping(target = "id", ignore = true) // Ignore id if it's not supposed to be updated
    void updateUserFromProfile(ProfileRequest request, @MappingTarget User user);

    CreatorDTO toCreatorDTO(User user);
}
