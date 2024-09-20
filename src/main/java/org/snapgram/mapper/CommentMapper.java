package org.snapgram.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.snapgram.dto.response.CommentDTO;
import org.snapgram.entity.database.Comment;

import java.util.List;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface CommentMapper {

    @Mapping(source = "user", target = "creator")
    @Mapping(source = "parentComment.id", target = "parentCommentId")
    CommentDTO toDTO(Comment comment);

    List<CommentDTO> toDTOs(List<Comment> comments);
}

