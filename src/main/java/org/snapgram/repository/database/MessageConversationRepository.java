package org.snapgram.repository.database;

import org.snapgram.entity.database.message.Conversation;
import org.snapgram.enums.ConversationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageConversationRepository extends JpaRepository<Conversation, UUID> {
    Optional<Conversation> findByIdAndTypeAndIsDeletedIsFalse(UUID id, ConversationType type);
}