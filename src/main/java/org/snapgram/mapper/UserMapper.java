package org.snapgram.mapper;
import org.mapstruct.Mapper;
import org.snapgram.entity.User;
import org.snapgram.model.request.SignupRequest;
import org.snapgram.model.response.UserDTO;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDTO(User user);
    User toEntity(SignupRequest request);
}
