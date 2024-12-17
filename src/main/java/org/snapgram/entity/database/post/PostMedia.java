package org.snapgram.entity.database.post;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.snapgram.entity.database.generator.UUIDGenerator;
import org.snapgram.enums.MediaType;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "post_media")
public class PostMedia {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", type = UUIDGenerator.class)
    @Column(updatable = false, columnDefinition = "char(36)")
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(length = 255, nullable = false)
    private String url;

    @Column(name = "cloudinary_public_id",length = 255)
    private String cloudinaryPublicId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaType type;


    @Column(name = "is_deleted",  columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isDeleted = false;
}

