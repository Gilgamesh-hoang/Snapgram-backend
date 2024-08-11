package org.snapgram.entity.elasticsearch;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.util.UUID;

@Data
@Builder
@Document(indexName = "user")
@Setting(settingPath = "/settings-elastic.json")
public class UserDocument {
    @Id
    private UUID id;
    @Field
    private String nickname;
    @Field
    private String email;
    @Field
    private String fullName;
    @Field
    private Boolean isActive;
    @Field
    private Boolean isDeleted;
}
