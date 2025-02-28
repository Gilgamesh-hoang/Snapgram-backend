package org.snapgram.service.message.factory;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.enums.ConversationType;
import org.snapgram.service.message.strategy.GroupStrategy;
import org.snapgram.service.message.strategy.MessageStrategy;
import org.snapgram.service.message.strategy.UserStrategy;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class MessageFactory {
    ApplicationContext context;

    public MessageStrategy get(ConversationType type) {
        return switch (type) {
            case GROUP -> context.getBean(GroupStrategy.class);
            case USER -> context.getBean(UserStrategy.class);
            default -> throw new IllegalArgumentException("Invalid type: " + type);
        };
    }
}


