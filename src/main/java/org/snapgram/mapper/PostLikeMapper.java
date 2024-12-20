package org.snapgram.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.snapgram.dto.PostLikeDTO;
import org.snapgram.entity.database.post.PostLike;

@Mapper(componentModel = "spring", uses = {UserMapper.class, PostMapper.class})
public interface PostLikeMapper {

    @Mapping(source = "user", target = "user")
    PostLikeDTO toDTO(PostLike like);
}

