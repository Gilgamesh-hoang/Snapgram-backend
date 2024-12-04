package org.snapgram.entity.database.timeline;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class TimelineId implements Serializable {
    private UUID userId;
    private UUID postId;

    // hashCode và equals để Hibernate nhận diện khóa chính
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimelineId that = (TimelineId) o;
        return userId.equals(that.userId) && postId.equals(that.postId);
    }

    @Override
    public int hashCode() {
        return userId.hashCode() + postId.hashCode();
    }
}
