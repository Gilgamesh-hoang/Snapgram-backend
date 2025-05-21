package org.snapgram.repository.database;

import org.snapgram.entity.database.message.Conversation;
import org.snapgram.enums.ConversationType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageConversationRepository extends JpaRepository<Conversation, UUID> {
    Optional<Conversation> findByIdAndTypeAndIsDeletedIsFalse(UUID id, ConversationType type);

    @Query("""
                SELECT c FROM Participant p
                JOIN p.conversation c
                WHERE p.user.id = :userId
                AND c.type = :type
                AND c.isDeleted = false
            """)
    List<Conversation> findAllByUser(@Param("userId") UUID userId,
                                     @Param("type") ConversationType type,
                                     Pageable pageable);
}