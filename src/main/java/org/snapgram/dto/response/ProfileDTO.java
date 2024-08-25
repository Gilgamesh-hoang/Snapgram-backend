package org.snapgram.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ProfileDTO extends UserDTO implements Serializable {
    private int postNumber;
    private int followeeNumber;
    private int followerNumber;

}
