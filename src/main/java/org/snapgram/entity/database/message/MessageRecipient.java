package org.snapgram.entity.database.message;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.snapgram.entity.database.generator.UUIDGenerator;

import java.util.UUID;

@Data
@Entity
@Builder
@Table(name = "message_recipient")
@AllArgsConstructor
@NoArgsConstructor
public class MessageRecipient {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", type = UUIDGenerator.class)
    @Column(updatable = false, columnDefinition = "char(36)")
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @ManyToOne
    @JoinColumn(name = "participant_id")
    private Participant participant;

    @Column(name = "is_read", columnDefinition = "TINYINT(1) DEFAULT 0")
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "is_deleted",  columnDefinition = "TINYINT(1) DEFAULT 0")
    @Builder.Default
    private Boolean isDeleted = false;
}
