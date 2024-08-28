package org.snapgram.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
public class TagDTO implements Serializable {
    @JsonIgnore
    private UUID id;
    private String name;
}
