package org.snapgram.repository.database;

import org.snapgram.entity.database.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IUserRepository extends JpaRepository<User, UUID> {

}
