package org.snapgram.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.snapgram.enums.Gender;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserInfoDTO extends UserDTO implements Serializable {
    private int postNumber;
    private int followeeNumber;
    private int followerNumber;
    private String bio;
    private Gender gender;
    @JsonIgnore
    private String activeCode;
    @JsonIgnore
    private Timestamp createdAt;
}
