package org.snapgram.service.post;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.CustomUserSecurity;
import org.snapgram.dto.request.PostRequest;
import org.snapgram.dto.response.PostDTO;
import org.snapgram.dto.response.PostMetricDTO;
import org.snapgram.entity.database.Post;
import org.snapgram.entity.database.PostMedia;
import org.snapgram.entity.database.Tag;
import org.snapgram.entity.database.User;
import org.snapgram.exception.ResourceNotFoundException;
import org.snapgram.mapper.PostMapper;
import org.snapgram.repository.database.PostRepository;
import org.snapgram.service.redis.IRedisService;
import org.snapgram.service.tag.ITagService;
import org.snapgram.service.user.IUserService;
import org.snapgram.util.UserSecurityHelper;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
    IPostSaveService postSaveService;

    @Override
    @Transactional
    @Async
    public CompletableFuture<PostDTO> createPost(PostRequest request, MultipartFile[] media) {
        request.setCaption(request.getCaption().trim());
        request.getTags().replaceAll(String::trim);

        CustomUserSecurity user = UserSecurityHelper.getCurrentUser();

        List<Tag> tagEntity = tagService.saveAll(request.getTags());
        // save post to database
        Post post = Post.builder()
                .user(User.builder().id(user.getId()).build())
                .caption(request.getCaption())
                .isDeleted(false)
                .likeCount(0)
                .commentCount(0)
                .tags(tagEntity)
                .build();
        postRepository.save(post);
        if (media != null) {
            List<PostMedia> postMediaList = postMediaService.savePostMedia(media, post.getId());
            post.setMedia(postMediaList);
        }
        return CompletableFuture.completedFuture(postMapper.toDTO(post));
    }

    @Override
    @Transactional
    public PostDTO updatePost(PostRequest request, MultipartFile[] media) {
        Post postEntity = postRepository.findById(request.getId()).orElse(null);
        CustomUserSecurity currentUser = UserSecurityHelper.getCurrentUser();

        if (postEntity == null) {
            throw new IllegalArgumentException("Post not found");
        }

        // If the current user is not the owner of the post, throw an exception
        if (!postEntity.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("You are not the owner of this post");
        }

        // Create a new list to hold the tags
        List<Tag> tagEntities = new ArrayList<>();
        List<String> tags = request.getTags();
        if (tags != null) {
            tags.replaceAll(String::trim);
            // Save all the tags to the database and add them to the list
            tagEntities = tagService.saveAll(tags);
        }
        postEntity.setTags(tagEntities);
        postEntity.setCaption(request.getCaption());

        // If there are any media to remove, remove them asynchronously
        if (request.getRemoveMedia() != null && !request.getRemoveMedia().isEmpty()) {
            CompletableFuture.runAsync(() -> postMediaService.removeMedia(request.getRemoveMedia()));
        }

        // If there are any media to add, add them
        if (media != null) {
            List<PostMedia> postMediaList = postMediaService.savePostMedia(media, postEntity.getId());
            postEntity.setMedia(postMediaList);
        }

        // Save the updated post entity to the database
        postRepository.save(postEntity);
        return postMapper.toDTO(postEntity);
    }


    @Override
    public void savePost(UUID postId) {
        boolean isExists = postRepository.existsById(postId);
        if (!isExists) {
            throw new ResourceNotFoundException("Post not found");
        }
        postSaveService.savePost(postId);
    }

    @Override
    public void unsavedPost(UUID postId) {
        boolean isExists = postRepository.existsById(postId);
        if (!isExists) {
            throw new ResourceNotFoundException("Post not found");
        }
        postSaveService.unsavedPost(postId);
    }


    @Override
    public PostMetricDTO like(UUID postId) {
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            throw new ResourceNotFoundException("Post not found");
        }
        postLikeService.like(postId);
        post.setLikeCount(postLikeService.countByPost(postId));
        postRepository.save(post);

        return PostMetricDTO.builder().likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .build();
    }

    @Override
    public PostMetricDTO unlike(UUID postId) {
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            throw new ResourceNotFoundException("Post not found");
        }
        postLikeService.unlike(postId);
        post.setLikeCount(postLikeService.countByPost(postId));
        postRepository.save(post);
        return PostMetricDTO.builder().likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .build();
    }

    @Override
    public boolean isExist(UUID postId) {
        Example<Post> example = Example.of(Post.builder().id(postId).isDeleted(false).build());
        return postRepository.exists(example);
    }

    @Override
    @Transactional
    public void updateCommentCount(UUID postId, int count) {
        postRepository.updateCommentCount(postId, count);
    }

    @Override
    public int countByUser(UUID userId) {
        Example<Post> example = Example.of(
                Post.builder().user(User.builder().id(userId).build()).isDeleted(false).build()
        );
        return (int) postRepository.count(example);
    }

    @Override
    public List<PostDTO> getPostsByUser(String nickname, Pageable pageable) {
//        String redisKey = RedisKeyUtil.getUserPostKey(nickname, pageable.getPageNumber(), pageable.getPageSize());
//        List<PostDTO> results = redisService.getList(redisKey);

//        if (results != null && !results.isEmpty()) {
//            return results;
//        }
        //select in database
        List<PostDTO> results;
        Example<Post> example = Example.of(
                Post.builder().user(
                        User.builder().id(userService.findByNickname(nickname).getId()).isDeleted(false).isActive(true).build()
                ).isDeleted(false).build()
        );
        Page<Post> posts = postRepository.findAll(example, pageable);
        // save to redis
        results = postMapper.toDTOs(posts.getContent());
        CustomUserSecurity currentUser = UserSecurityHelper.getCurrentUser();
        results.forEach(postDTO -> {
            boolean isLiked = postLikeService.isPostLikedByUser(postDTO.getId(), currentUser.getId());
            postDTO.setLiked(isLiked);
            boolean isSaved = postSaveService.isPostSaveByUser(postDTO.getId(), currentUser.getId());
            postDTO.setSaved(isSaved);
        });

//        if (!results.isEmpty()) {
//            redisService.saveList(redisKey, results);
//            redisService.setTimeout(redisKey, 5, TimeUnit.DAYS);
//        }
        return results;
    }

    @Override
    public PostDTO getPostById(UUID id) {
//        String redisKey = RedisKeyUtil.getPostKey(id);
        PostDTO result;
//        PostDTO result = redisService.getValue(redisKey, PostDTO.class);
//        if (result != null) {
//            return result;
//        }
        Post post = postRepository.findById(id).orElse(null);
        if (post == null) {
            return null;
        }
        CustomUserSecurity currentUser = UserSecurityHelper.getCurrentUser();
        boolean isLiked = postLikeService.isPostLikedByUser(post.getId(), currentUser.getId());
        boolean isSaved = postSaveService.isPostSaveByUser(post.getId(), currentUser.getId());
        result = postMapper.toDTO(post);
        result.setLiked(isLiked);
        result.setSaved(isSaved);
//        redisService.saveValue(redisKey, result);
//        redisService.setTimeout(redisKey, 30, TimeUnit.SECONDS);
        return result;
    }


}
