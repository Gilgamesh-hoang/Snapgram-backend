package org.snapgram.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.CustomUserSecurity;
import org.snapgram.dto.request.AddParticipantsRequest;
import org.snapgram.dto.request.CreateGroupMessageRequest;
import org.snapgram.dto.response.ConversationDTO;
import org.snapgram.dto.response.CreatorDTO;
import org.snapgram.dto.response.MessageResponse;
import org.snapgram.dto.response.ResponseObject;
import org.snapgram.enums.ConversationType;
import org.snapgram.service.message.IMessageService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("${API_PREFIX}/messages")
@Validated
public class MessageController {
    IMessageService messageService;

    /**
     * Retrieves the participants of a conversation.
     *
     * @param conversationId the ID of the conversation
     * @return a ResponseObject containing a list of CreatorDTOs representing the participants
     */
    @GetMapping("/conversations/participants")
    public ResponseObject<List<CreatorDTO>> getParticipants(
            @RequestParam("conversationId") @NotNull UUID conversationId
    ) {
        List<CreatorDTO> participants = messageService.getParticipants(conversationId);
        return new ResponseObject<>(HttpStatus.OK, participants);
    }

    /**
     * Retrieves messages in a conversation.
     *
     * @param conversationId the ID of the conversation
     * @param type the type of the conversation
     * @param pageNumber the page number for pagination
     * @param pageSize the page size for pagination
     * @return a ResponseObject containing a list of MessageResponses
     */
    @GetMapping()
    public ResponseObject<List<MessageResponse>> getMessagesInConversation(
            @RequestParam("conversationId") @NotNull UUID conversationId,
            @RequestParam("type") @NotNull ConversationType type,
            @RequestParam(value = "pageNum", defaultValue = "1") @Min(0) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "20") @Min(0) @Max(50) Integer pageSize
    ) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
        List<MessageResponse> messages = messageService.getMessages(conversationId, type, pageable);
        return new ResponseObject<>(HttpStatus.OK, messages);
    }

    /**
     * Retrieves the count of unread conversations for the current user.
     *
     * @param currentUser the current authenticated user
     * @return a ResponseObject containing the count of unread conversations
     */
    @GetMapping("/unread-count")
    public ResponseObject<Integer> getUnreadConversations(@AuthenticationPrincipal CustomUserSecurity currentUser) {
        int count = messageService.getUnreadConversationCount(currentUser.getId());
        return new ResponseObject<>(HttpStatus.OK, count);
    }


    /**
     * Retrieves information about a conversation.
     *
     * @param conversationId the ID of the conversation
     * @param type the type of the conversation
     * @return a ResponseObject containing a ConversationDTO with the conversation information
     */
    @GetMapping("/conversations/info")
    public ResponseObject<ConversationDTO> getInfo(@RequestParam("conversationId") @NotNull UUID conversationId,
                                                   @RequestParam("type") @NotNull ConversationType type
    ) {
        return new ResponseObject<>(HttpStatus.OK, messageService.getConservationInfo(conversationId, type));
    }

    /**
     * Retrieves the conversations of the current user.
     *
     * @param user the current authenticated user
     * @param pageNumber the page number for pagination
     * @param pageSize the page size for pagination
     * @return a ResponseObject containing a list of MessageResponses
     */
    @GetMapping("/conversations")
    public ResponseObject<List<MessageResponse>> getMessagedUsers(
            @AuthenticationPrincipal CustomUserSecurity user,
            @RequestParam(value = "pageNum", defaultValue = "1") @Min(0) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "20") @Min(0) @Max(20) Integer pageSize
    ) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
        List<MessageResponse> users = messageService.getConversations(pageable, user.getId());
        return new ResponseObject<>(HttpStatus.OK, users);
    }

    /**
     * Marks a conversation as read.
     *
     * @param conversationId the ID of the conversation
     * @param currentUser the current authenticated user
     * @return a ResponseObject indicating whether the operation was successful
     */
    @GetMapping("/mark-as-read")
    public ResponseObject<Boolean> markAsRead(@RequestParam("conversationId") @NotNull UUID conversationId,
                                              @AuthenticationPrincipal CustomUserSecurity currentUser
    ) {
        boolean isRead = messageService.markAsRead(conversationId, currentUser.getId());
        return new ResponseObject<>(HttpStatus.OK, isRead);
    }

    /**
     * Creates a new group conversation.
     *
     * @param request the request containing the details for the new group
     * @param currentUser the current authenticated user
     * @return a ResponseObject containing a ConversationDTO with the new group information
     */
    @PostMapping("/conversations/group")
    public ResponseObject<ConversationDTO> createGroup(@RequestBody @Valid CreateGroupMessageRequest request,
                                                       @AuthenticationPrincipal CustomUserSecurity currentUser) {
        if (request.getParticipantIds().length < 2) {
            return new ResponseObject<>(HttpStatus.BAD_REQUEST, "Group must have at least 2 participants");
        }
        return new ResponseObject<>(HttpStatus.OK, messageService.createGroup(request, currentUser.getId()));
    }

    /**
     * Adds participants to an existing group conversation.
     *
     * @param request the request containing the details of the participants to add
     * @param currentUser the current authenticated user
     * @return a ResponseObject indicating the operation was successful
     */
    @PostMapping("/conversations/groups/participants")
    public ResponseObject<Void> addParticipantsToGroup(
            @RequestBody @Valid AddParticipantsRequest request,
            @AuthenticationPrincipal CustomUserSecurity currentUser) {
        messageService.addParticipantsToGroup(request.getConversationId(), request.getParticipantIds(), currentUser.getId());
        return new ResponseObject<>(HttpStatus.OK);
    }

}
