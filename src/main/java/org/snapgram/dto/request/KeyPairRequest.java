package org.snapgram.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.snapgram.dto.KeyPair;

import java.util.UUID;

@Data
@AllArgsConstructor
public class KeyPairRequest {
    private UUID userId;
    private KeyPair keyPair;
}
