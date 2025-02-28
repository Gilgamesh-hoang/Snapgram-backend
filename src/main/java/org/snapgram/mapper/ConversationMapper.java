package org.snapgram.mapper;

import org.mapstruct.Mapper;
import org.snapgram.dto.response.ConversationDTO;
import org.snapgram.entity.database.message.Conversation;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface ConversationMapper {

    ConversationDTO toDTO(Conversation conversation);
}
