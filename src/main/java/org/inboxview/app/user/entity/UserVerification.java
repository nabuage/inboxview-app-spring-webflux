package org.inboxview.app.user.entity;

import java.time.OffsetDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "user_verification")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UserVerification {
    @Id
    @Column("user_verification_id")
    private Long verificationId;

    @Column("user_id")
    private Long userId;

    @Column("code")
    private String code;

    @Column("attempt_count")
    private Long attemptCount;

    @Column("date_verified")
    private OffsetDateTime dateVerified;

    @Column("date_added")
    private OffsetDateTime dateAdded;

    @Column("date_deleted")
    private OffsetDateTime dateDeleted;    
}
