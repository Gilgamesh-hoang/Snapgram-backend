package org.snapgram.dto;

import jakarta.validation.constraints.NotNull;
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
public class AffinityDTO implements Serializable {
    @NotNull
    private UUID followerId;
    @NotNull
    private UUID followeeId;
    private float closeness;
}
