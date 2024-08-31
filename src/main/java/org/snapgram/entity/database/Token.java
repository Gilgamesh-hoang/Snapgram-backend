package org.snapgram.entity.database;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Date;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "token")
public class Token {

    @Id
    @Column(columnDefinition = "char(36)")
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID refreshTokenId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "expire_at")
//    @Temporal(TemporalType.TIMESTAMP)
    private Date expireAt;

}