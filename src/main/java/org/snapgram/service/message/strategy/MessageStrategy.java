package org.snapgram.service.message.strategy;

import com.corundumstudio.socketio.SocketIOClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.dto.request.MessageRequest;
import org.snapgram.dto.response.ConversationDTO;
import org.snapgram.dto.response.MessageResponse;
import org.snapgram.entity.database.message.Participant;
import org.snapgram.exception.ResourceNotFoundException;
import org.snapgram.exception.UnauthorizedActionException;
import org.snapgram.repository.database.MessageParticipantRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PROTECTED)
public abstract class MessageStrategy {
    MessageParticipantRepository participantRepository;

    /**
     * Sends a message.
     *
     * @param senderClient the client sending the message
     * @param request the message request containing the message details
     * @return a MessageResponse object containing the details of the sent message
     */
    public abstract MessageResponse sendMessage(SocketIOClient senderClient, MessageRequest request);

    /**
     * Retrieves information about a conversation.
     *
     * @param conversationId the ID of the conversation
     * @return a ConversationDTO object containing the conversation information
     */
    public abstract ConversationDTO getConservationInfo(UUID conversationId);

    /**
     * Retrieves messages in a conversation.
     *
     * @param conversationId the ID of the conversation
     * @param pageable the pagination information
     * @return a list of MessageResponse objects representing the messages
     */
    public abstract List<MessageResponse> getMessages(UUID conversationId, Pageable pageable);

    /**
     * Validates the user's participation in a conversation.
     *
     * @param conversationId the ID of the conversation
     * @param currentUserId the ID of the current authenticated user
     * @return a list of Participant objects representing the participants excluding the current user
     * @throws ResourceNotFoundException if the conversation is not found
     * @throws UnauthorizedActionException if the user is not part of the conversation
     */
    protected List<Participant> validateUserParticipation(UUID conversationId, UUID currentUserId) {
        List<Participant> participants = participantRepository.findAllByConversationId(conversationId);
        if (participants.isEmpty()) {
            throw new ResourceNotFoundException("Conversation not found");
        }

        if (participants.stream().noneMatch(p -> p.getUser().getId().equals(currentUserId))) {
            throw new UnauthorizedActionException("User is not part of the conversation");
        }
        return participants.stream().filter(p -> !p.getUser().getId().equals(currentUserId)).toList();
    }

}
