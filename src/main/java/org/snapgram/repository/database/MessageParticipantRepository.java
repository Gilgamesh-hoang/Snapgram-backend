package org.snapgram.repository.database;

import org.snapgram.entity.database.message.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageParticipantRepository extends JpaRepository<Participant, UUID> {

    @Query("""
            SELECT p FROM Participant p WHERE p.user.id = :otherUserId
            AND p.conversation.id IN (SELECT p2.conversation.id FROM Participant p2 WHERE p2.user.id = :currentUserId)
            """)
    Participant findConversationByUserIds(UUID currentUserId, UUID otherUserId);

    @Query("""
            SELECT p FROM Participant p WHERE p.user.id not in :excludeUserIds
            AND p.conversation.id = :conversationId
            """)
    List<Participant> findAllByConversationIdAndExcludeUserIds(UUID conversationId, List<UUID> excludeUserIds);

    List<Participant> findAllByConversationId(UUID conversationId);

}
