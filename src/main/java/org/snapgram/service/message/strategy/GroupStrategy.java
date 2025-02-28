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
import org.snapgram.exception.ResourceNotFoundException;
import org.snapgram.mapper.ConversationMapper;
import org.snapgram.mapper.MessageMapper;
import org.snapgram.repository.database.MessageConversationRepository;
import org.snapgram.repository.database.MessageParticipantRepository;
import org.snapgram.repository.database.MessageRecipientRepository;
import org.snapgram.repository.database.MessageRepository;
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
public class GroupStrategy extends MessageStrategy {
    UserSocketManager userSocketManager;
    ObjectMapper objectMapper;
    MessageRepository messageRepository;
    MessageRecipientRepository recipientRepository;
    IUserService userService;
    MessageConversationRepository conversationRepository;
    ConversationMapper conversationMapper;
    MessageMapper messageMapper;

    public GroupStrategy(MessageParticipantRepository participantRepository,
                         UserSocketManager userSocketManager,
                         ObjectMapper objectMapper,
                         MessageRepository messageRepository,
                         MessageRecipientRepository recipientRepository,
                         IUserService userService,
                         MessageConversationRepository conversationRepository,
                         ConversationMapper conversationMapper,
                         MessageMapper messageMapper) {
        super(participantRepository);
        this.userSocketManager = userSocketManager;
        this.objectMapper = objectMapper;
        this.messageRepository = messageRepository;
        this.recipientRepository = recipientRepository;
        this.userService = userService;
        this.conversationRepository = conversationRepository;
        this.conversationMapper = conversationMapper;
        this.messageMapper = messageMapper;
    }

    @Override
    @Transactional
    public MessageResponse sendMessage(SocketIOClient senderClient, MessageRequest request) {
        if (request.getConversationId() == null)
            throw new IllegalArgumentException("Conversation ID is required");

        Conversation conversation = conversationRepository
                .findByIdAndTypeAndIsDeletedIsFalse(request.getConversationId(), ConversationType.GROUP)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        List<Participant> participants = validateUserParticipation(request.getConversationId(), request.getSenderId());

        // Save the message and get the response
        MessageResponse response = saveMessage(request, participants, conversation);

        // Get the recipient's socket client
        List<SocketIOClient> recipientClients = userSocketManager.getUserSocketsByUserIds(participants.stream().map(p -> p.getUser().getId()).toList());
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

    private MessageResponse saveMessage(@Valid MessageRequest request, List<Participant> participants, Conversation conversation) {
        Message message = Message.builder()
                .sender(User.builder().id(request.getSenderId()).build())
                .content(request.getContent())
                .type(request.getContentType())
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();
        messageRepository.save(message);

        List<MessageRecipient> recipients = participants.stream().map(participant -> MessageRecipient.builder()
                .message(message)
                .participant(participant)
                .build()).toList();
        recipientRepository.saveAll(recipients);

        return buildMessageResponse(message, conversation);
    }

    private MessageResponse buildMessageResponse(Message message, Conversation conversation) {
        return MessageResponse.builder()
                .id(message.getId())
                .sender(userService.getCreatorById(message.getSender().getId()))
                .content(message.getContent())
                .contentType(message.getType())
                .conversation(conversationMapper.toDTO(conversation))
                .createdAt(message.getCreatedAt())
                .build();
    }

    @Override
    public ConversationDTO getConservationInfo(UUID id) {
        CustomUserSecurity currentUser = UserSecurityHelper.getCurrentUser();
        validateUserParticipation(id, currentUser.getId());
        Conversation conversation = conversationRepository.findById(id).orElse(null);
        return conversationMapper.toDTO(conversation);
    }

    @Override
    public List<MessageResponse> getMessages(UUID conversationId, Pageable pageable) {
        CustomUserSecurity currentUser = UserSecurityHelper.getCurrentUser();
        validateUserParticipation(conversationId, currentUser.getId());
        List<MessageResultDTO> messages = recipientRepository.findGroupMessagesByConversationId(conversationId, currentUser.getId(), pageable);

        return messageMapper.toResponses(messages);
    }

}
