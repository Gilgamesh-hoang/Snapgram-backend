package org.snapgram.service.post;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.CloudinaryMedia;
import org.snapgram.dto.CustomUserSecurity;
import org.snapgram.dto.request.PostRequest;
import org.snapgram.dto.response.PostDTO;
import org.snapgram.dto.response.UserDTO;
import org.snapgram.entity.database.post.Post;
import org.snapgram.entity.database.post.PostMedia;
import org.snapgram.entity.database.post.Tag;
import org.snapgram.entity.database.user.User;
import org.snapgram.exception.ResourceNotFoundException;
import org.snapgram.kafka.producer.PostProducer;
import org.snapgram.kafka.producer.RedisProducer;
import org.snapgram.kafka.producer.NewsfeedProducer;
import org.snapgram.mapper.PostMapper;
import org.snapgram.repository.database.PostRepository;
import org.snapgram.service.cloudinary.ICloudinarySignatureService;
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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

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
    PostProducer postProducer;
    RedisProducer redisProducer;
    NewsfeedProducer newsfeedProducer;
    IPostSaveService postSaveService;
    ICloudinarySignatureService signatureService;

    private Post savePost(PostRequest request) {
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
        return post;
    }

    @Override
    @Transactional
    @Async
    public CompletableFuture<PostDTO> createPost(PostRequest request, MultipartFile[] media) {
        CustomUserSecurity user = UserSecurityHelper.getCurrentUser();
        Post post = savePost(request);
        if (media != null) {
            List<PostMedia> postMediaList = postMediaService.savePostMedia(media, post.getId());
            post.setMedia(postMediaList);
        }

        // delete cache
        deletePostCache(user.getNickname());

        newsfeedProducer.sendPostCreatedMessage(user.getId(), post.getId(), post.getCreatedAt());

        return CompletableFuture.completedFuture(postMapper.toDTO(post));
    }

    @Override
    @Transactional
    public PostDTO createPost(PostRequest request) {
        List<CloudinaryMedia> validMedia = request.getMedia().stream()
                .filter(signatureService::verifySignature)
                .toList();
        if (validMedia.isEmpty()) {
            throw new IllegalArgumentException("Invalid media");
        }
        CustomUserSecurity user = UserSecurityHelper.getCurrentUser();
        Post post = savePost(request);

        List<PostMedia> postMediaList = postMediaService.savePostMedia(validMedia, post.getId());
        post.setMedia(postMediaList);

        // delete cache
        deletePostCache(user.getNickname());

        newsfeedProducer.sendPostCreatedMessage(user.getId(), post.getId(), post.getCreatedAt());

        return postMapper.toDTO(post);
    }



    private void deletePostCache(String nickname) {
        String redisKey = RedisKeyUtil.getUserPostKey(nickname, 0, 0);
        redisProducer.sendDeleteByKey(redisKey.substring(0, redisKey.indexOf("page")));
    }

    @Override
    @Transactional
    public PostDTO updatePost(PostRequest request, MultipartFile[] media) {
        Post postEntity = validateAndPreparePostForUpdate(request);

        // If there are any media to add, add them
        if (media != null) {
            List<PostMedia> postMediaList = postMediaService.savePostMedia(media, postEntity.getId());
            postEntity.setMedia(postMediaList);
        }

        // Save the updated post entity to the database
        postRepository.save(postEntity);
        PostDTO result = postMapper.toDTO(postEntity);

        // save to redis
        redisProducer.sendSaveValue(RedisKeyUtil.getPostKey(postEntity.getId()), result, 1L, TimeUnit.DAYS);

        return result;
    }

    @Override
    @Transactional
    public PostDTO updatePost(PostRequest request) {
        Post postEntity = validateAndPreparePostForUpdate(request);

        // If there are any media to add, add them
        if (request.getMedia() != null) {
            List<CloudinaryMedia> validMedia = request.getMedia().stream()
                    .filter(signatureService::verifySignature)
                    .toList();
            if (!validMedia.isEmpty()) {
                List<PostMedia> postMediaList = postMediaService.savePostMedia(validMedia, postEntity.getId());
                postEntity.setMedia(postMediaList);
            }
        }

        // Save the updated post entity to the database
        postRepository.save(postEntity);
        PostDTO result = postMapper.toDTO(postEntity);

        // save to redis
        redisProducer.sendSaveValue(RedisKeyUtil.getPostKey(postEntity.getId()), result, 1L, TimeUnit.DAYS);

        return result;
    }


    private Post validateAndPreparePostForUpdate(PostRequest request) {
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
            postProducer.sendRemoveMedia(request.getRemoveMedia());
        }

        return postEntity;
    }


    @Override
    public void savePost(UUID postId) {
        updateSavePost(postId, postSaveService::savePost);
    }

    @Override
    public void unsavedPost(UUID postId) {
        updateSavePost(postId, postSaveService::unsavedPost);
    }

    private void updateSavePost(UUID postId, Consumer<UUID> action) {
        boolean isExists = postRepository.existsById(postId);
        if (!isExists) {
            throw new ResourceNotFoundException("Post not found");
        }
        action.accept(postId);
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
    @Async
    @Transactional
    public CompletableFuture<Void> updateLikeCount(UUID postId, int likeCount) {
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            throw new ResourceNotFoundException("Post not found");
        }
        postRepository.updateLikeCount(postId, post.getLikeCount() + likeCount);
        return CompletableFuture.completedFuture(null);

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
        int page, size;
        try {
            page = pageable.getPageNumber();
            size = pageable.getPageSize();
        } catch (UnsupportedOperationException e) {
            // unpaged
            page = size = -1;
        }
        String redisKey = RedisKeyUtil.getUserPostKey(nickname, page, size);
        List<PostDTO> results = redisService.getList(redisKey, PostDTO.class);

        if (results != null && !results.isEmpty()) {
            return results;
        }
        //select in database
        Example<Post> example = Example.of(
                Post.builder().user(
                        User.builder().id(userService.getByNickname(nickname).getId()).isDeleted(false).isActive(true).build()
                ).isDeleted(false).build()
        );
        Page<Post> posts = postRepository.findAll(example, pageable);
        results = postMapper.toDTOs(posts.getContent());

        // save to redis
        redisProducer.sendSaveList(redisKey, results, 1L, TimeUnit.MINUTES);
        return results;
    }

    @Override
    public List<PostDTO> getPostsByUser(UUID userId, Pageable pageable) {
        UserDTO user = userService.getById(userId);
        return getPostsByUser(user.getNickname(), pageable);
    }

    @Override
    public PostDTO getPostById(UUID id) {
        String redisKey = RedisKeyUtil.getPostKey(id);
        PostDTO result = redisService.getValue(redisKey, PostDTO.class);
        if (result != null) {
            return result;
        }
        Post post = postRepository.findById(id).orElse(null);

        if (post != null) {
            result = postMapper.toDTO(post);
        }
        // save to redis
        redisProducer.sendSaveValue(redisKey, result, 1L, TimeUnit.MINUTES);
        return result;
    }

    @Override
    public List<PostDTO> getPostsByIds(List<UUID> postIds) {
        List<Post> posts = postRepository.findAllById(postIds);

        if (!posts.isEmpty()) {
            return postMapper.toDTOs(posts);
        }
        return new ArrayList<>();
    }

    @Override
    public List<PostDTO> getPostsByUsersAndAfter(List<UUID> userIds, Timestamp time) {
        List<Post> posts = postRepository.findAllByUserIdsAndAfter(userIds, time);
        return postMapper.toDTOs(posts);
    }
}
