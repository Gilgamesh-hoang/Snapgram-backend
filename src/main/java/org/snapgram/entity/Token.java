package org.snapgram.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.snapgram.entity.generator.UUIDGenerator;

import java.util.UUID;

@Data
@Entity
@Table(name = "token")
public class Token {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", type = UUIDGenerator.class)
    @Column(updatable = false, columnDefinition = "char(36)")
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 36, nullable = false)
    private String token;

    @Column(length = 30)
    private String type;

    @Column(name = "expired", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean expired = false;

}
