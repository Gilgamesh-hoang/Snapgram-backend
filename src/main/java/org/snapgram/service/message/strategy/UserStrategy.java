package org.snapgram.service.message.strategy;

import com.corundumstudio.socketio.SocketIOClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.dto.CustomUserSecurity;
import org.snapgram.dto.MessageResultDTO;
import org.snapgram.dto.request.MessageRequest;
import org.snapgram.dto.response.ConversationDTO;
import org.snapgram.dto.response.MessageResponse;
import org.snapgram.entity.database.message.Conversation;
import org.snapgram.entity.database.message.Message;
import org.snapgram.entity.database.message.MessageRecipient;
import org.snapgram.entity.database.message.Participant;
import org.snapgram.entity.database.user.User;
import org.snapgram.enums.ConversationType;
import org.snapgram.enums.MessageType;
import org.snapgram.exception.ResourceNotFoundException;
import org.snapgram.mapper.ConversationMapper;
import org.snapgram.mapper.MessageMapper;
import org.snapgram.repository.database.MessageConversationRepository;
import org.snapgram.repository.database.MessageParticipantRepository;
import org.snapgram.repository.database.MessageRecipientRepository;
import org.snapgram.repository.database.MessageRepository;
import org.snapgram.service.follow.IAffinityService;
import org.snapgram.service.user.IUserService;
import org.snapgram.socket.UserSocketManager;
import org.snapgram.util.AppConstant;
import org.snapgram.util.UserSecurityHelper;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Component
@Validated
@Slf4j
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class UserStrategy extends MessageStrategy {
    UserSocketManager userSocketManager;
    ObjectMapper objectMapper;
    MessageRepository messageRepository;
    MessageConversationRepository conversationRepository;
    MessageRecipientRepository recipientRepository;
    IUserService userService;
    ConversationMapper conversationMapper;
    MessageMapper messageMapper;
    IAffinityService affinityService;

    public UserStrategy(MessageParticipantRepository participantRepository,
                        UserSocketManager userSocketManager,
                        ObjectMapper objectMapper,
                        MessageRepository messageRepository,
                        MessageConversationRepository conversationRepository,
                        MessageRecipientRepository recipientRepository,
                        IUserService userService,
                        IAffinityService affinityService,
                        ConversationMapper conversationMapper,
                        MessageMapper messageMapper) {
        super(participantRepository);
        this.userSocketManager = userSocketManager;
        this.objectMapper = objectMapper;
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.recipientRepository = recipientRepository;
        this.userService = userService;
        this.affinityService = affinityService;
        this.conversationMapper = conversationMapper;
        this.messageMapper = messageMapper;
    }

    @Override
    public ConversationDTO getConservationInfo(UUID conversationId) {
        // check current user is part of the conversation
        CustomUserSecurity currentUser = UserSecurityHelper.getCurrentUser();
        return getConservationInfo(currentUser.getId(), conversationId);
    }

    private ConversationDTO getConservationInfo(UUID currentUserId, UUID conversationId) {
        // get the conversation
        Conversation conversation = conversationRepository.findByIdAndTypeAndIsDeletedIsFalse(conversationId, ConversationType.USER)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        // check current user is part of the conversation
        Participant participant = validateUserParticipation(conversation.getId(), currentUserId).get(0);

        return ConversationDTO.builder()
                .id(conversation.getId())
                .type(conversation.getType())
                .name(participant.getUser().getNickname())
                .avatar(participant.getUser().getAvatarUrl())
                .build();
    }


    @Override
    public List<MessageResponse> getMessages(UUID conversationId, Pageable pageable) {
        CustomUserSecurity currentUser = UserSecurityHelper.getCurrentUser();
        validateUserParticipation(conversationId, currentUser.getId());
        List<MessageResultDTO> messages = recipientRepository.findUserMessagesByConversationId(conversationId, pageable);
        return messageMapper.toResponses(messages);
    }


    @Override
    @Transactional
    public MessageResponse sendMessage(MessageRequest request) {
        // Save the message and get the response
        MessageResponse response = saveMessage(request);

        // Retrieve the recipient's ID from the response
        UUID recipientId = response.getRecipient().getId();

        // Increase affinity between sender and recipient
        affinityService.increaseAffinity(request.getSenderId(), recipientId);
        // Get the recipient's socket client
        List<SocketIOClient> recipientClients = userSocketManager.getUserSockets(recipientId);
        // If the recipient is connected, send the message
        if (!recipientClients.isEmpty()) {
            recipientClients.forEach(client -> {
                try {
                    client.sendEvent(AppConstant.RECEIVE_MESSAGE_EVENT, objectMapper.writeValueAsString(response));
                } catch (JsonProcessingException e) {
                    log.error("Error while sending message to recipient", e);
                }
            });
        }

        return response;
    }

    /**
     * Saves the message to the database.
     *
     * @param request the message request data
     * @return the saved message response
     */
    private MessageResponse saveMessage(@Valid MessageRequest request) {
        if (request.getConversationId() != null) {
            return handleExistingConversation(request);
        } else if (request.getRecipientId() != null) {
            return handleNewConversation(request);
        } else {
            throw new IllegalArgumentException("Invalid request");
        }
    }

    /**
     * Handles an existing conversation by saving a new message to it.
     *
     * @param request the message request containing the conversation ID, sender ID, content, and content type
     * @return a MessageResponse object containing the details of the saved message
     * @throws ResourceNotFoundException if the conversation is not found
     */
    private MessageResponse handleExistingConversation(MessageRequest request) {
        // Retrieve the conversation by ID, throw an exception if not found
        ConversationDTO conversation = this.getConservationInfo(request.getSenderId(), request.getConversationId());

        // Validate the sender's participation in the conversation
        Participant participant = validateUserParticipation(conversation.getId(), request.getSenderId()).get(0);

        // Create and save the message
        Message message = createMessage(request.getSenderId(), request.getContent(), request.getContentType());
        message = messageRepository.save(message);

        // Create and save the message recipient
        MessageRecipient messageRecipient = createMessageRecipient(message, participant);
        recipientRepository.save(messageRecipient);

        // Build and return the message response
        return buildMessageResponse(message, participant.getUser(), conversation);
    }

    /**
     * Handles a new conversation by creating it and saving the initial message.
     *
     * @param request the message request containing the sender ID, recipient ID, content, and content type
     * @return a MessageResponse object containing the details of the saved message
     */
    private MessageResponse handleNewConversation(MessageRequest request) {
        // Check if the conversation already exists between the sender and recipient
        Participant participant = participantRepository.findConversationByUserIds(request.getSenderId(), request.getRecipientId());
        if (participant != null) {
            return handleExistingConversation(request);
        }

        // Create and save a new conversation
        Conversation conversation = Conversation.builder()
                .type(ConversationType.USER)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();
        conversation = conversationRepository.save(conversation);

        // Create and save participants for the sender and recipient
        Participant senderParticipant = createParticipant(conversation, request.getSenderId());
        Participant recipientParticipant = createParticipant(conversation, request.getRecipientId());
        participantRepository.saveAll(List.of(recipientParticipant, senderParticipant));

        // Create and save the message
        Message message = createMessage(request.getSenderId(), request.getContent(), request.getContentType());
        message = messageRepository.save(message);

        // Create and save the message recipient
        MessageRecipient messageRecipient = createMessageRecipient(message, recipientParticipant);
        recipientRepository.save(messageRecipient);

        ConversationDTO conv = ConversationDTO.builder()
                .id(conversation.getId())
                .type(conversation.getType())
                .name(recipientParticipant.getUser().getNickname())
                .avatar(recipientParticipant.getUser().getAvatarUrl())
                .build();
        // Build and return the message response
        return buildMessageResponse(message, recipientParticipant.getUser(), conv);
    }

    private Participant createParticipant(Conversation conversation, UUID userId) {
        return Participant.builder()
                .conversation(conversation)
                .user(User.builder().id(userId).build())
                .build();
    }

    private Message createMessage(UUID senderId, String content, MessageType contentType) {
        return Message.builder()
                .sender(User.builder().id(senderId).build())
                .content(content)
                .type(contentType)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();
    }

    private MessageRecipient createMessageRecipient(Message message, Participant participant) {
        return MessageRecipient.builder()
                .message(message)
                .participant(participant)
                .build();
    }

    private MessageResponse buildMessageResponse(Message message, User recipient, ConversationDTO conversation) {
        return MessageResponse.builder()
                .id(message.getId())
                .sender(userService.getCreatorById(message.getSender().getId()))
                .recipient(userService.getCreatorById(recipient.getId()))
                .content(message.getContent())
                .contentType(message.getType())
                .conversation(conversation)
                .createdAt(message.getCreatedAt())
                .build();
    }

}