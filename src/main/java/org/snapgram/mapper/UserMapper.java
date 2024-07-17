package org.snapgram.mapper;
import org.mapstruct.Mapper;
import org.snapgram.entity.User;
import org.snapgram.model.UserDTO;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDTO(User user);
}
