package org.snapgram.repository.database;

import org.snapgram.dto.MessageResultDTO;
import org.snapgram.entity.database.message.MessageRecipient;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRecipientRepository extends JpaRepository<MessageRecipient, UUID> {

    @Query(value = """
            WITH latest_message AS (
                SELECT
                    m.id AS message_id,
                    m.sender_id,
                    mp.user_id AS recipient_id,
                    m.content,
                    m.`type`,
                    m.created_at,
                    mr.is_read,
                    mp.conversation_id,
                    ROW_NUMBER() OVER (PARTITION BY mp.conversation_id ORDER BY m.created_at DESC) AS rn
                FROM message m
                JOIN message_recipient mr ON m.id = mr.message_id
                JOIN message_participant mp ON mr.participant_id = mp.id
            )
            SELECT
                lm.message_id,
                lm.sender_id,
                lm.recipient_id,
                lm.content,
                lm.`type` AS message_type,
                lm.created_at AS message_created_at,
                mc.id AS conversation_id,
                mc.`type` AS conversation_type,
                mc.`name` AS conversation_name,
                lm.is_read
            FROM message_conversation mc
            JOIN message_participant mp ON mc.id = mp.conversation_id
            LEFT JOIN latest_message lm ON mc.id = lm.conversation_id AND lm.rn = 1
            WHERE mp.user_id = :userId
            ORDER BY COALESCE(mc.created_at,lm.created_at) DESC
            LIMIT :limit OFFSET :offset;
            """
            , nativeQuery = true)
    List<Object[]> findLatestMessagesInConversations(@Param("userId") String userId,
                                                     @Param("limit") int limit,
                                                     @Param("offset") int offset);

    @Query("""
                SELECT new org.snapgram.dto.MessageResultDTO(
                    m.id,
                    m.sender,
                    mp.user ,
                    m.content,
                    m.type,
                    m.createdAt,
                    mp.conversation.id,
                    mp.conversation.type,
                    mp.conversation.name,
                    mr.isRead
                )
                FROM MessageRecipient mr
                INNER JOIN mr.message m
                INNER JOIN mr.participant mp
                WHERE
                    mp.conversation.id = :conversationId AND m.isDeleted = false
                ORDER BY m.createdAt DESC
            """)
    List<MessageResultDTO> findUserMessagesByConversationId(@Param("conversationId") UUID conversationId, Pageable pageable);

    @Query("""
                SELECT mr
                FROM MessageRecipient mr
                WHERE mr.message.id = :messageId AND mr.participant.user.id = :userId
            """)
    Optional<MessageRecipient> findRecipientByMessageAndUser(@Param("messageId") UUID messageId, @Param("userId") UUID userId);

    @Query("""
                SELECT new org.snapgram.dto.MessageResultDTO(
                    m.id,
                    m.sender,
                    m.content,
                    m.type,
                    m.createdAt,
                    mp.conversation.id,
                    mp.conversation.type,
                    mp.conversation.name
                )
                FROM MessageRecipient mr
                INNER JOIN mr.message m
                INNER JOIN mr.participant mp
                WHERE
                    mp.conversation.id = :conversationId AND m.isDeleted = false AND (
                        m.sender.id = :currentUserId OR (mp.user.id = :currentUserId AND m.sender.id <> :currentUserId)
                    )
                GROUP BY m.id
                ORDER BY m.createdAt DESC
            """)
    List<MessageResultDTO> findGroupMessagesByConversationId(@Param("conversationId") UUID conversationId,
                                                             @Param("currentUserId") UUID currentUserId,
                                                             Pageable pageable);

    @Query("""
                SELECT mr.id
                FROM MessageRecipient mr
                INNER JOIN mr.participant mp
                WHERE mp.conversation.id = :conversationInfo
                AND mp.user.id = :currentUserId
                AND mr.isRead = false
            """)
    List<UUID> findUnreadMessageIds(UUID conversationInfo, UUID currentUserId);

    @Modifying
    @Query("""
                UPDATE MessageRecipient mr
                SET mr.isRead = true
                WHERE mr.id IN :ids
            """)
    int markAsReadByIds(@Param("ids") List<UUID> ids);

}