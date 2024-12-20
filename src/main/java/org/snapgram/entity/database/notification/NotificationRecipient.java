package org.snapgram.entity.database.notification;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.snapgram.entity.database.generator.UUIDGenerator;
import org.snapgram.entity.database.user.User;

import java.util.UUID;

@Entity
@Table(name = "notification_recipient")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationRecipient {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", type = UUIDGenerator.class)
    @Column(updatable = false, columnDefinition = "char(36)")
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "notification_entity_id", nullable = false)
    private NotificationEntity notificationEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Column(name = "is_read", columnDefinition = "TINYINT(1) DEFAULT 0")
    @Builder.Default
    private Boolean isRead = false;
}