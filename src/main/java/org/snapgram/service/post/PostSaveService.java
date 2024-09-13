package org.snapgram.service.post;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.CustomUserSecurity;
import org.snapgram.dto.response.PostDTO;
import org.snapgram.entity.database.Post;
import org.snapgram.entity.database.Saved;
import org.snapgram.entity.database.User;
import org.snapgram.mapper.PostMapper;
import org.snapgram.repository.database.PostSaveRepository;
import org.snapgram.util.UserSecurityHelper;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostSaveService implements IPostSaveService {
    PostSaveRepository postSaveRepository;
    PostMapper postMapper;
    IPostLikeService postLikeService;

    @Override
    public boolean isPostSaveByUser(UUID postId, UUID userId) {
        User user = User.builder().id(userId).build();
        Post post = Post.builder().id(postId).build();
        Example<Saved> example = Example.of(Saved.builder().post(post).user(user).build());
        return postSaveRepository.exists(example);
    }

    @Override
    public List<PostDTO> getSavedPostsByUser(UUID userId, Pageable pageable) {
        Example<Saved> example = Example.of(Saved.builder().user(User.builder().id(userId).build()).build());
        List<Saved> savedPosts = postSaveRepository.findAll(example, pageable).getContent();
        List<PostDTO> results = postMapper.toDTOs(savedPosts.stream().map(Saved::getPost).toList());
        results.forEach(post -> {
            post.setSaved(true);
            boolean isLiked = postLikeService.isPostLikedByUser(post.getId(), userId);
            post.setLiked(isLiked);
        });
        return results;
    }

    @Override
    public void savePost(UUID postId) {
        CustomUserSecurity currentUser = UserSecurityHelper.getCurrentUser();
        if (isPostSavedByUser(postId, currentUser.getId())) {
            return;
        }
        Saved saved = Saved.builder()
                .post(Post.builder().id(postId).build())
                .user(User.builder().id(currentUser.getId()).build())
                .build();
        postSaveRepository.save(saved);
    }

    @Override
    public boolean isPostSavedByUser(UUID postId, UUID id) {
        User user = User.builder().id(id).build();
        Post post = Post.builder().id(postId).build();
        Example<Saved> example = Example.of(Saved.builder().post(post).user(user).build());
        return postSaveRepository.exists(example);
    }

    @Override
    @Transactional
    public void unsavedPost(UUID postId) {
        CustomUserSecurity currentUser = UserSecurityHelper.getCurrentUser();
        postSaveRepository.deleteByPostIdAndUserId(postId, currentUser.getId());
    }
}
