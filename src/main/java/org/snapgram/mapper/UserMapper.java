package org.snapgram.mapper;

import org.mapstruct.*;
import org.snapgram.dto.GooglePojo;
import org.snapgram.dto.request.ProfileRequest;
import org.snapgram.dto.request.SignupRequest;
import org.snapgram.dto.response.CreatorDTO;
import org.snapgram.dto.response.UserInfoDTO;
import org.snapgram.dto.response.UserDTO;
import org.snapgram.entity.database.user.User;
import org.snapgram.entity.elasticsearch.UserDocument;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "userInfo.gender", target = "gender")
    @Mapping(source = "userInfo.bio", target = "bio")
    UserInfoDTO toUserInfoDTO(User user);

    @Named("userToUserDTO")
    UserDTO toDTO(User user);

    @IterableMapping(qualifiedByName = "userToUserDTO")
    List<UserDTO> toDTOs(List<User> users);

    UserDTO toDTO(UserDocument user);

    UserDocument toUserDocument(User user);

    Collection<UserDTO> toDTOs(Collection<UserDocument> users);

    @Mapping(source = "name", target = "fullName")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "picture", target = "avatarUrl")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "nickname", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
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
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    User toEntity(SignupRequest request);

    @Mapping(target = "id", ignore = true) // Ignore id if it's not supposed to be updated
    @Mapping(source = "gender", target = "userInfo.gender")
    @Mapping(source = "bio", target = "userInfo.bio")
    void updateUserFromProfile(ProfileRequest request, @MappingTarget User user);

    CreatorDTO toCreatorDTO(User user);

    CreatorDTO toCreatorDTO(UserDTO user);

    void updateUserDocumentFromUser(@MappingTarget UserDocument userDocument, User user);
}
