package org.snapgram.repository.database;

import org.snapgram.entity.database.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface IUserRepository extends JpaRepository<User, UUID> {

    @Query(value = "SELECT u FROM User u WHERE u.id NOT IN :exceptIds " +
            "AND u.isActive = true AND u.isDeleted = false " +
            "ORDER BY FUNCTION('RAND') LIMIT :limit")
    List<User> findRandomUsers(@Param("limit") int limit, @Param("exceptIds") List<UUID> exceptIds);


    @Query("SELECT u FROM Follow f JOIN User u ON u.id = f.followingUser.id " +
            "WHERE u.isActive = true AND u.isDeleted = false " +
            "AND f.followedUser.id = :followedUserId")
    List<User> findFollowers(@Param("followedUserId") UUID followedUserId);

}
