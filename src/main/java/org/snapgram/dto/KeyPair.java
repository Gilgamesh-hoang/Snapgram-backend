package org.snapgram.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KeyPair {
    private String publicKeyAT;
    private String privateKeyAT;
    private String publicKeyRT;
    private String privateKeyRT;
}
