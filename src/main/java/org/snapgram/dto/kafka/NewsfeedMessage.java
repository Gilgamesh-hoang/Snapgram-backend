package org.snapgram.dto.kafka;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsfeedMessage implements Serializable {
    @NotNull
    private NewsfeedType type;
    @NotNull
    private Object data;

   public enum NewsfeedType {
        POST_CREATED,
        FOLLOW_CREATED,
        UNFOLLOW
    }
}
