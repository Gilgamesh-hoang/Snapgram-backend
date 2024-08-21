package org.snapgram.service.post;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.response.PostDTO;
import org.snapgram.dto.response.UserDTO;
import org.snapgram.entity.database.Post;
import org.snapgram.entity.database.PostMedia;
import org.snapgram.entity.database.Tag;
import org.snapgram.entity.database.User;
import org.snapgram.mapper.PostMapper;
import org.snapgram.repository.database.PostRepository;
import org.snapgram.service.tag.ITagService;
import org.snapgram.service.user.IUserService;
import org.snapgram.util.UserSecurityHepler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostService implements IPostService {
    IUserService userService;
    PostRepository postRepository;
    PostMapper postMapper;
    ITagService tagService;
    PostMediaService postMediaService;

    @Override
    @Transactional
    public PostDTO createPost(String caption, MultipartFile[] media, List<String> tags) {
        caption = caption.trim();
        tags.replaceAll(s -> s.trim().toLowerCase());

        String email = UserSecurityHepler.getCurrentUser().getUsername();
        UserDTO user = userService.findByEmail(email);

        List<Tag> tagEntity = tagService.saveAll(tags);
        // save post to database
        Post post = Post.builder()
                .user(User.builder().id(user.getId()).build())
                .caption(caption)
                .isDeleted(false)
                .likeCount(0)
                .tags(tagEntity)
                .build();
        postRepository.save(post);
        if (media != null) {
            List<PostMedia> postMediaList = postMediaService.savePostMedia(media, post);
            post.setMedia(postMediaList);
        }
        return postMapper.toDTO(post);
    }
}
