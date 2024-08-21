package org.snapgram.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class TagDTO {
    private UUID id;
    private String name;
}
