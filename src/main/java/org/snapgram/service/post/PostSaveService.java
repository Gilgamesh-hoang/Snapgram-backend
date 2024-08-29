package org.snapgram.service.post;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.CustomUserSecurity;
import org.snapgram.entity.database.Post;
import org.snapgram.entity.database.Saved;
import org.snapgram.entity.database.User;
import org.snapgram.repository.database.PostSaveRepository;
import org.snapgram.util.UserSecurityHelper;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostSaveService implements IPostSaveService {
    PostSaveRepository postSaveRepository;

    @Override
    public boolean isPostSaveByUser(UUID postId, UUID userId) {
        User user = User.builder().id(userId).build();
        Post post = Post.builder().id(postId).build();
        Example<Saved> example = Example.of(Saved.builder().post(post).user(user).build());
        return postSaveRepository.exists(example);
    }

    @Override
    @Transactional
    public void savePost(UUID postId, boolean isSaved) {
        CustomUserSecurity currentUser = UserSecurityHelper.getCurrentUser();
        if (isSaved) {
            Saved saved = Saved.builder()
                    .post(Post.builder().id(postId).build())
                    .user(User.builder().id(currentUser.getId()).build())
                    .build();
            postSaveRepository.save(saved);
        } else {
            postSaveRepository.deleteByPostIdAndUserId(postId, currentUser.getId());
        }
    }
}
