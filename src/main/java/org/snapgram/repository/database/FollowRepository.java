package org.snapgram.repository.database;

import org.snapgram.entity.database.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FollowRepository extends JpaRepository<Follow, UUID> {


}
