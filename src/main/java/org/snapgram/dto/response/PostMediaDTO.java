package org.snapgram.dto.response;

import lombok.Data;
import org.snapgram.enums.MediaType;

import java.io.Serializable;
import java.util.UUID;

@Data
public class PostMediaDTO implements Serializable {

    private UUID id;

    private String url;

    private MediaType type;
}

