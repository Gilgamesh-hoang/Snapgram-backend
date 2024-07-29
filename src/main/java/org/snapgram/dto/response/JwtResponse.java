package org.snapgram.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String type;
    private long expiredTime;

    public JwtResponse(String token, long expiredTime) {
        this.token = token;
        this.expiredTime = expiredTime;
        this.type = "jwt";
    }

}