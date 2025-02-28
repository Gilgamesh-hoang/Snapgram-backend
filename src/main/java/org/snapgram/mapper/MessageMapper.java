package org.snapgram.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.snapgram.dto.MessageResultDTO;
import org.snapgram.dto.response.ConversationDTO;
import org.snapgram.dto.response.MessageResponse;
import org.snapgram.entity.database.user.User;
import org.snapgram.enums.ConversationType;
import org.snapgram.enums.MessageType;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface MessageMapper {
    @Mapping(target = "sender", source = "sender")
    @Mapping(target = "recipient", source = "recipient")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "contentType", source = "contentType")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "isRead", expression = "java(mess.isRead())")
    @Mapping(target = "conversation", expression = "java(mapConversation(mess))")
    MessageResponse toResponse(MessageResultDTO mess);

    default ConversationDTO mapConversation(MessageResultDTO mess) {
        if (mess.getConversationId() == null) {
            return null;
        }
        return ConversationDTO.builder()
                .id(mess.getConversationId())
                .name(mess.getConversationName())
                .type(mess.getConversationType())
                .build();
    }

    List<MessageResponse> toResponses(List<MessageResultDTO> messages);

    default List<MessageResultDTO> toMessageResultDTO(List<Object[]> rawResults) {
        return rawResults.stream()
                .map(this::mapToMessageResultDTO)
                .toList();
    }

    private MessageResultDTO mapToMessageResultDTO(Object[] obj) {
        return new MessageResultDTO(
                parseUUID(obj[0]), // messageId
                parseUser(obj[1]), // sender
                parseUser(obj[2]), // recipient
                parseString(obj[3]), // content
                parseEnum(obj[4], MessageType.class), // contentType
                parseTimestamp(obj[5]), // createdAt
                parseUUID(obj[6]), // groupId
                parseEnum(obj[7], ConversationType.class), // groupType
                parseString(obj[8]), // groupName
                parseBoolean(obj[9]) // isRead
        );
    }

    private UUID parseUUID(Object obj) {
        return obj != null ? UUID.fromString((String) obj) : null;
    }

    private User parseUser(Object obj) {
        return obj != null ? new User(UUID.fromString((String) obj)) : null;
    }

    private String parseString(Object obj) {
        return obj != null ? (String) obj : null;
    }

    private <T extends Enum<T>> T parseEnum(Object obj, Class<T> enumClass) {
        return obj != null ? Enum.valueOf(enumClass, (String) obj) : null;
    }

    private Timestamp parseTimestamp(Object obj) {
        return obj != null ? (Timestamp) obj : null;
    }

    private boolean parseBoolean(Object obj) {
        return obj == null || (boolean) obj;
    }

}