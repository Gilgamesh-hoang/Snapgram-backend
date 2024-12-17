package org.snapgram.repository.database;

import org.snapgram.entity.database.post.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {

    Tag findByName(String name);
}
