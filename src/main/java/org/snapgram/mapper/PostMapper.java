package org.snapgram.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.snapgram.dto.response.PostDTO;
import org.snapgram.entity.database.post.Post;

import java.util.List;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface PostMapper {
    @Mapping(source = "user", target = "creator")
    PostDTO toDTO(Post post);

    List<PostDTO> toDTOs(List<Post> posts);
}
