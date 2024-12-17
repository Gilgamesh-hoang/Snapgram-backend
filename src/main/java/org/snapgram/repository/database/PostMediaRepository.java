package org.snapgram.repository.database;

import org.snapgram.entity.database.post.PostMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PostMediaRepository extends JpaRepository<PostMedia, UUID> {


}
