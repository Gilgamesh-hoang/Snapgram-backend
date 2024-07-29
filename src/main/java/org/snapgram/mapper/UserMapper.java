package org.snapgram.mapper;
import org.mapstruct.Mapper;
import org.snapgram.entity.User;
import org.snapgram.dto.request.SignupRequest;
import org.snapgram.dto.response.UserDTO;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDTO(User user);
    User toEntity(SignupRequest request);
}
