package org.snapgram.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.snapgram.dto.GooglePojo;
import org.snapgram.dto.request.SignupRequest;
import org.snapgram.dto.response.PostDTO;
import org.snapgram.dto.response.UserDTO;
import org.snapgram.entity.database.Post;
import org.snapgram.entity.database.User;
import org.snapgram.entity.elasticsearch.UserDocument;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring")
public interface PostMapper {
    PostDTO toDTO(Post post);

    List<PostDTO> toDTOs(List<Post> posts);
}
