package org.snapgram.dto;

import lombok.Builder;
import lombok.Data;
import org.snapgram.dto.response.CreatorDTO;
import org.snapgram.dto.response.PostDTO;

import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
public class PostLikeDTO implements Serializable {
    private UUID id;
    private PostDTO post;
    private CreatorDTO user;
}
