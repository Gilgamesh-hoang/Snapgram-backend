package org.snapgram.service.message;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.dto.CloudinaryMedia;
import org.snapgram.dto.CustomUserSecurity;
import org.snapgram.dto.MessageResultDTO;
import org.snapgram.dto.request.CreateGroupMessageRequest;
import org.snapgram.dto.request.MessageRequest;
import org.snapgram.dto.response.ConversationDTO;
import org.snapgram.dto.response.CreatorDTO;
import org.snapgram.dto.response.MessageResponse;
import org.snapgram.dto.response.UserDTO;
import org.snapgram.entity.database.message.Conversation;
import org.snapgram.entity.database.message.MessageRecipient;
import org.snapgram.entity.database.message.Participant;
import org.snapgram.entity.database.user.User;
import org.snapgram.enums.ConversationType;
import org.snapgram.exception.ResourceNotFoundException;
import org.snapgram.exception.UnauthorizedActionException;
import org.snapgram.kafka.producer.RedisProducer;
import org.snapgram.mapper.ConversationMapper;
import org.snapgram.mapper.MessageMapper;
import org.snapgram.mapper.UserMapper;
import org.snapgram.repository.database.MessageConversationRepository;
import org.snapgram.repository.database.MessageParticipantRepository;
import org.snapgram.repository.database.MessageRecipientRepository;
import org.snapgram.service.cloudinary.ICloudinarySignatureService;
import org.snapgram.service.message.factory.MessageFactory;
import org.snapgram.service.message.strategy.MessageStrategy;
import org.snapgram.service.redis.IRedisService;
import org.snapgram.service.user.IUserService;
import org.snapgram.socket.UserSocketManager;
import org.snapgram.util.AppConstant;
import org.snapgram.util.RedisKeyUtil;
import org.snapgram.util.UserSecurityHelper;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class MessageService implements IMessageService, DataListener<MessageRequest> {
    UserSocketManager userSocketManager;
    MessageFactory messageFactory;
    MessageRecipientRepository recipientRepository;
    MessageConversationRepository conversationRepository;
    MessageParticipantRepository participantRepository;
    MessageMapper messageMapper;
    UserMapper userMapper;
    ObjectMapper objectMapper;
    ConversationMapper conversationMapper;
    IUserService userService;
    ICloudinarySignatureService signatureService;
    IRedisService redisService;
    RedisProducer redisProducer;

    @Override
    public void onData(SocketIOClient senderClient, MessageRequest request, AckRequest ackSender) {
        // Retrieve the sender's user ID from the socket manager
        List<SocketIOClient> sockets = userSocketManager.getUserSockets(request.getSenderId());

        if (sockets.stream().noneMatch(socket -> socket.getSessionId().equals(senderClient.getSessionId()))) {
            // Send an error event if the sender ID is invalid
            senderClient.sendEvent(AppConstant.ERROR_EVENT, "Invalid sender");
            return;
        }

        // Get the appropriate message strategy based on the conversation type
        MessageStrategy messageStrategy = messageFactory.get(request.getConversationType());

        // Send the message using the selected strategy
        MessageResponse response = messageStrategy.sendMessage(request);

        // Send an acknowledgment if requested
        if (ackSender.isAckRequested()) {
            try {
                ackSender.sendAckData(objectMapper.writeValueAsString(response));
            } catch (JsonProcessingException e) {
                log.error("Error sending acknowledgment", e);
            }
        }
    }

    @Override
    public List<MessageResponse> getConversations(Pageable pageable, UUID currentUser) {
        // Retrieve the latest messages in conversations for the current user with pagination
        List<Object[]> rawResults = recipientRepository.findLatestMessagesInConversations(
                currentUser.toString(), pageable.getPageSize(), pageable.getPageNumber());

        // Map the raw results to MessageResultDTO objects
        List<MessageResultDTO> messages = messageMapper.toMessageResultDTO(rawResults);

        // Convert MessageResultDTO objects to MessageResponse objects
        List<MessageResponse> results = messages.stream()
                .map(messageMapper::toResponse)
                .toList();

        // Enrich each message with additional information
        results.forEach(message ->
                enrichMessage(message, currentUser)
        );

        return results;
    }

    @Override
    public List<ConversationDTO> getConversationsByType(UUID currentUser, ConversationType type) {
        String redisKey = RedisKeyUtil.getConversationsKey(currentUser, type);
        List<ConversationDTO> results = redisService.getList(redisKey, ConversationDTO.class);

        if (results != null) {
            return results;
        }

        List<Conversation> conversations = conversationRepository.findAllByUser(currentUser, type, Pageable.unpaged());
        results = conversations.stream()
                .map(conversationMapper::toDTO)
                .toList();

        // Cache the results in Redis
        redisProducer.sendSaveList(redisKey, results, null, null);

        return results;
    }

    /**
     * Enriches a message with additional information.
     *
     * @param message the message to enrich
     * @param currentUser the ID of the current authenticated user
     */
    private void enrichMessage(MessageResponse message, UUID currentUser) {
        // Set the sender information if available
        if (message.getSender() != null) {
            message.setSender(userService.getCreatorById(message.getSender().getId()));
        }

        // Set the recipient information if the conversation type is USER and recipient is available
        if (message.getConversation().getType().equals(ConversationType.USER) && message.getRecipient() != null) {
            message.setRecipient(userService.getCreatorById(message.getRecipient().getId()));
        }

        // Set the conversation information
        message.setConversation(getConservationInfo(message.getConversation().getId(), message.getConversation().getType()));

        // Handle read status for group conversations
        if (message.getConversation().getType().equals(ConversationType.GROUP) && message.getSender() != null) {
            if (message.getSender().getId().equals(currentUser)) {
                message.setRead(true);
            } else {
                recipientRepository.findRecipientByMessageAndUser(message.getId(), currentUser)
                        .ifPresent(recipient -> message.setRead(recipient.getIsRead()));
            }
        }

        // Set the creation date for group conversations if the message ID is null
        if (message.getConversation().getType().equals(ConversationType.GROUP) && message.getId() == null) {
            conversationRepository.findById(message.getConversation().getId())
                    .ifPresent(conversation -> message.setCreatedAt(conversation.getCreatedAt()));
        }
    }

    @Override
    public ConversationDTO getConservationInfo(UUID conversationId, ConversationType type) {
        // Get the appropriate message strategy based on the conversation type
        MessageStrategy messageStrategy = messageFactory.get(type);
        return messageStrategy.getConservationInfo(conversationId);
    }

    @Override
    public List<MessageResponse> getMessages(UUID conversationId, ConversationType type, Pageable pageable) {
        // Get the appropriate message strategy based on the conversation type
        MessageStrategy messageStrategy = messageFactory.get(type);
        return messageStrategy.getMessages(conversationId, pageable);
    }


    @Transactional
    @Override
    public boolean markAsRead(UUID conversationId, UUID currentUserId) {
        // Find unread message IDs for the given conversation and user
        List<UUID> ids = recipientRepository.findUnreadMessageIds(conversationId, currentUserId);

        // Mark messages as read if there are any unread messages
        if (!ids.isEmpty()) {
            return recipientRepository.markAsReadByIds(ids) > 0;
        }
        return false;
    }

    @Override
    public int getUnreadConversationCount(UUID currentUserId) {
        // Retrieve the latest messages in conversations for the current user
        List<Object[]> rawResults = recipientRepository.findLatestMessagesInConversations(currentUserId.toString(), Integer.MAX_VALUE, 0);
        List<MessageResultDTO> messages = messageMapper.toMessageResultDTO(rawResults);

        // Filter messages to exclude those sent by the current user
        messages = messages.stream().filter(message -> {
            if (message.getId() == null) {
                return false;
            }
            return !message.getSender().getId().equals(currentUserId);
        }).toList();

        int count = 0;
        // Count unread messages for USER and GROUP conversation types

        for (MessageResultDTO message : messages) {
            if (message.getConversationType().equals(ConversationType.USER) && !message.isRead()) {
                count++;
            } else if (message.getConversationType().equals(ConversationType.GROUP)) {
                MessageRecipient messageRecipient = recipientRepository.findRecipientByMessageAndUser(message.getId(), currentUserId).orElse(null);
                if (messageRecipient != null && !messageRecipient.getIsRead()) {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    @Transactional
    public ConversationDTO createGroup(CreateGroupMessageRequest request, UUID ownerId) {
        // Verify the avatar signature if provided
        CloudinaryMedia avatar = request.getAvatar();
        if (avatar != null && !signatureService.verifySignature(avatar)) {
            throw new IllegalArgumentException("Invalid avatar signature");
        }

        // Create a new conversation entity
        Conversation conversation = Conversation.builder()
                .name(request.getName())
                .avatar(avatar != null ? avatar.getUrl() : null)
                .type(ConversationType.GROUP)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .owner(User.builder().id(ownerId).build())
                .build();
        conversation = conversationRepository.save(conversation);

        // Retrieve users by their IDs and validate the participant count
        List<UserDTO> usersByIds = userService.getUsersByIds(request.getParticipantIds());
        if (usersByIds.size() < 2) {
            throw new IllegalArgumentException("Group must have at least 2 participants");
        }
        if (usersByIds.size() != request.getParticipantIds().length) {
            throw new IllegalArgumentException("Some users not found");
        }

        // Add participants to the conversation
        List<Participant> participants = new ArrayList<>();
        for (UserDTO user : usersByIds) {
            participants.add(Participant.builder()
                    .conversation(conversation)
                    .user(User.builder().id(user.getId()).build())
                    .build());
        }
        participants.add(Participant.builder()
                .conversation(conversation)
                .user(conversation.getOwner())
                .build());
        participantRepository.saveAll(participants);
        return conversationMapper.toDTO(conversation);
    }

    @Override
    public List<CreatorDTO> getParticipants(UUID conversationId) {
        // Get the current authenticated user
        CustomUserSecurity currentUser = UserSecurityHelper.getCurrentUser();
        List<Participant> participants = participantRepository.findAllByConversationId(conversationId);
        if (participants.isEmpty()) {
            throw new ResourceNotFoundException("Conversation not found");
        }

        // Validate that the current user is part of the conversation
        if (participants.stream().noneMatch(p -> p.getUser().getId().equals(currentUser.getId()))) {
            throw new IllegalArgumentException("User is not part of the conversation");
        }

        // Map participants to CreatorDTO objects
        return participants.stream().map(participant -> userMapper.toCreatorDTO(participant.getUser())).toList();
    }

    @Override
    @Transactional
    public void addParticipantsToGroup(UUID conversationId, List<UUID> newParticipantIds, UUID requesterId) {
        // Retrieve the conversation and validate the requester
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        if (!conversation.getOwner().getId().equals(requesterId)) {
            throw new UnauthorizedActionException("Only the owner can add participants");
        }

        // Validate the new participants
        List<UUID> validUserIds = userService.getUsersByIds(newParticipantIds.toArray(UUID[]::new))
                .stream()
                .map(UserDTO::getId)
                .toList();

        if (validUserIds.size() != newParticipantIds.size()) {
            throw new ResourceNotFoundException("Some users were not found");
        }

        // Filter out existing participants
        Set<UUID> existingParticipantIds = participantRepository.findAllByConversationId(conversationId)
                .stream()
                .map(participant -> participant.getUser().getId())
                .collect(Collectors.toSet());

        List<UUID> uniqueNewParticipantIds = validUserIds.stream()
                .filter(userId -> !existingParticipantIds.contains(userId))
                .toList();

        if (uniqueNewParticipantIds.isEmpty()) {
            throw new IllegalArgumentException("All selected users are already participants");
        }

        // Add new participants to the conversation
        List<Participant> newParticipants = uniqueNewParticipantIds.stream()
                .map(userId -> Participant.builder()
                        .conversation(conversation)
                        .user(User.builder().id(userId).build())
                        .build())
                .toList();

        participantRepository.saveAll(newParticipants);

        userSocketManager.getUserSocketsByUserIds(uniqueNewParticipantIds)
                .forEach(socket -> {
                            socket.joinRoom(conversationId.toString());
                            log.info("User {} joined room {}", socket.getSessionId(), conversationId);
                        }
                );
    }

}
