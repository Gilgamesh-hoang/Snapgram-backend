package org.snapgram.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.snapgram.dto.request.SignupRequest;
import org.snapgram.dto.response.UserDTO;
import org.snapgram.entity.database.User;
import org.snapgram.entity.elasticsearch.UserDocument;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDTO(User user);
    UserDTO toDTO(UserDocument user);
    Collection<UserDTO> toDTOs(Collection<UserDocument> users);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "bio", ignore = true)
    @Mapping(target = "activeCode", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toEntity(SignupRequest request);
}
