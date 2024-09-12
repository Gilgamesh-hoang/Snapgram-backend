package org.snapgram.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeyPair implements Serializable {
    private String publicKeyAT;
    private String privateKeyAT;
    private String publicKeyRT;
    private String privateKeyRT;

}
