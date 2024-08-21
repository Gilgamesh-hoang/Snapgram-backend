package org.snapgram.repository.database;

import org.snapgram.entity.database.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {


}
