package org.snapgram.service.post;

import org.snapgram.dto.response.PostDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IPostService {
    PostDTO createPost(String caption, MultipartFile[] media, List<String> tags);
}
