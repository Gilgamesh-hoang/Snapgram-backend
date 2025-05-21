package org.snapgram.service.message;

import org.snapgram.dto.request.CreateGroupMessageRequest;
import org.snapgram.dto.response.ConversationDTO;
import org.snapgram.dto.response.CreatorDTO;
import org.snapgram.dto.response.MessageResponse;
import org.snapgram.enums.ConversationType;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface IMessageService {
    /**
     * Retrieves the conversations of the current user.
     *
     * @param pageable the pagination information
     * @param currentUser the ID of the current authenticated user
     * @return a list of MessageResponse objects representing the conversations
     */
    List<MessageResponse> getConversations(Pageable pageable, UUID currentUser);

    List<ConversationDTO> getConversationsByType(UUID currentUser, ConversationType type);

    /**
     * Retrieves information about a conversation.
     *
     * @param conversationId the ID of the conversation
     * @param type the type of the conversation
     * @return a ConversationDTO object containing the conversation information
     */
    ConversationDTO getConservationInfo(UUID conversationId, ConversationType type);

    /**
     * Retrieves messages in a conversation.
     *
     * @param conversationId the ID of the conversation
     * @param type the type of the conversation
     * @param pageable the pagination information
     * @return a list of MessageResponse objects representing the messages
     */
    List<MessageResponse> getMessages(UUID conversationId, ConversationType type, Pageable pageable);

    /**
     * Marks a conversation as read.
     *
     * @param conversationId the ID of the conversation
     * @param currentUserId the ID of the current authenticated user
     * @return true if the operation was successful, false otherwise
     */
    boolean markAsRead(UUID conversationId, UUID currentUserId);

    /**
     * Retrieves the count of unread conversations for the current user.
     *
     * @param currentUserId the ID of the current authenticated user
     * @return the count of unread conversations
     */
    int getUnreadConversationCount(UUID currentUserId);

    /**
     * Creates a new group conversation.
     *
     * @param request the request containing the details for the new group
     * @param ownerId the ID of the current authenticated user
     * @return a ConversationDTO object containing the new group information
     */
    ConversationDTO createGroup(CreateGroupMessageRequest request, UUID ownerId);

    /**
     * Retrieves the participants of a conversation.
     *
     * @param conversationId the ID of the conversation
     * @return a list of CreatorDTO objects representing the participants
     */
    List<CreatorDTO> getParticipants(UUID conversationId);

    /**
     * Adds participants to an existing group conversation.
     *
     * @param conversationId the ID of the conversation
     * @param newParticipantIds the list of IDs of the new participants
     * @param requesterId the ID of the current authenticated user
     */
    void addParticipantsToGroup(UUID conversationId, List<UUID> newParticipantIds, UUID requesterId);
}
