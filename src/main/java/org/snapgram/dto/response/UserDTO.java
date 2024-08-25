package org.snapgram.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.snapgram.enums.Gender;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO implements Serializable {
    private UUID id;
    private String nickname;
    private String fullName;
    private String email;
    private String avatarUrl;
    private String bio;
    private Gender gender;
    @JsonIgnore
    private String activeCode;
    @JsonIgnore
    private Timestamp createdAt;
}
