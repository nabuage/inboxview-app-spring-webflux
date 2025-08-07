package org.inboxview.app.user.entity;

import java.time.OffsetDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "refresh_token")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RefreshToken {
    @Id
    @Column("refresh_token_id")
    private Long refreshTokenId;

    @Column("refresh_token_guid")
    private String guid;

    @Column("access_token")
    private String accessToken;

    @Column("user_id")
    private Long userId;

    @Column("date_added")
    private OffsetDateTime dateAdded;

    @Column("expiration_date")
    private OffsetDateTime expirationDate;
}
