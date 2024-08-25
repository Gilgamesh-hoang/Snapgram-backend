package org.snapgram.service.post;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.CustomUserSecurity;
import org.snapgram.dto.response.PostDTO;
import org.snapgram.dto.response.UserDTO;
import org.snapgram.entity.database.Post;
import org.snapgram.entity.database.PostMedia;
import org.snapgram.entity.database.Tag;
import org.snapgram.entity.database.User;
import org.snapgram.mapper.PostMapper;
import org.snapgram.repository.database.PostRepository;
import org.snapgram.service.redis.IRedisService;
import org.snapgram.service.tag.ITagService;
import org.snapgram.service.user.IUserService;
import org.snapgram.util.RedisKeyUtil;
import org.snapgram.util.UserSecurityHelper;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostService implements IPostService {
    IUserService userService;
    PostRepository postRepository;
    PostMapper postMapper;
    ITagService tagService;
    PostMediaService postMediaService;
    IRedisService redisService;
    IPostLikeService postLikeService;

    @Override
    @Transactional
    @Async
    public CompletableFuture<PostDTO> createPost(String caption, MultipartFile[] media, List<String> tags) {
        caption = caption.trim();
        tags.replaceAll(s -> s.trim().toLowerCase());

        CustomUserSecurity user = UserSecurityHelper.getCurrentUser();

        List<Tag> tagEntity = tagService.saveAll(tags);
        // save post to database
        Post post = Post.builder()
                .user(User.builder().id(user.getId()).build())
                .caption(caption)
                .isDeleted(false)
                .likeCount(0)
                .commentCount(0)
                .tags(tagEntity)
                .build();
        postRepository.save(post);
        if (media != null) {
            List<PostMedia> postMediaList = postMediaService.savePostMedia(media, post);
            post.setMedia(postMediaList);
        }
        return CompletableFuture.completedFuture(postMapper.toDTO(post));
    }

    @Override
    public int countByUser(UUID userId) {
        Example<Post> example = Example.of(
                Post.builder().user(User.builder().id(userId).build())
                        .isDeleted(false).build()
        );
        return (int) postRepository.count(example);
    }

    @Override
    public List<PostDTO> getPostsByUser(String nickname, Pageable pageable) {
        String redisKey = RedisKeyUtil.getUserPostKey(nickname, pageable.getPageNumber(), pageable.getPageSize());
        List<PostDTO> results = redisService.getList(redisKey);

        if (results != null && !results.isEmpty()) {
            return results;
        }
        //select in database
        Example<Post> example = Example.of(
                Post.builder().user(
                        User.builder().id(userService.findByNickname(nickname).getId()).isDeleted(false).isActive(true).build()
                ).isDeleted(false).build()
        );

        Page<Post> posts = postRepository.findAll(example, pageable);
        // save to redis
        results = postMapper.toDTOs(posts.getContent());
        UserDTO user = userService.findByEmail(UserSecurityHelper.getCurrentUser().getUsername());
        results.forEach(postDTO -> {
            boolean isLiked = postLikeService.isPostLikedByUser(postDTO.getId(), user.getId());
            postDTO.setLiked(isLiked);
        });

        if (!results.isEmpty()) {
            redisService.saveList(redisKey, results);
            redisService.setTimeout(redisKey, 5, TimeUnit.DAYS);
        }
        return results;
    }
}
