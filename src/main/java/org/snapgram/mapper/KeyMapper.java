package org.snapgram.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.snapgram.dto.KeyPair;
import org.snapgram.dto.response.CommentDTO;
import org.snapgram.entity.database.Comment;
import org.snapgram.entity.database.Key;

import java.util.List;

@Mapper(componentModel = "spring")
public interface KeyMapper {
    Key toEntity(KeyPair key);

    KeyPair toDto(Key key);
}

