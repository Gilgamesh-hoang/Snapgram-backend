package org.snapgram.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostLikeUpdateMessage implements Serializable {
    private UUID postId;
    private Action action;

    public enum Action {
        INCREMENT,
        DECREMENT
    }
}
