package org.snapgram.mapper;

import org.mapstruct.Mapper;
import org.snapgram.dto.response.PostDTO;
import org.snapgram.entity.database.Post;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PostMapper {
    PostDTO toDTO(Post post);

    List<PostDTO> toDTOs(List<Post> posts);
}
