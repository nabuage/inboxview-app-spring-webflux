package org.inboxview.app.user.entity;

import java.time.OffsetDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "\"user\"")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class User {
    @Id
    @Column("user_id")
    private Long id;

    @Column("user_guid")
    private String guid;

    @Column("username")
    private String username;

    @Column("password")
    private String password;

    @Column("email")
    private String email;

    @Column("first_name")
    private String firstName;

    @Column("last_name")
    private String lastName;

    @Column("phone")
    private String phone;

    @Column("date_added")
    private OffsetDateTime dateAdded;

    @Column("date_updated")
    private OffsetDateTime dateUpdated;

    @Column("date_deleted")
    private OffsetDateTime dateDeleted;

    @Column("date_verified")
    private OffsetDateTime dateVerified;

    @Version
    private int version;

    @Column("password_reset_token")
    private String passwordResetToken;

    @Column("password_reset_date_requested")
    private OffsetDateTime passwordResetDateRequested;

    @Column("password_reset_count")
    private Long passwordResetCount;

    @Column("password_date_reset")
    private OffsetDateTime passwordDateReset;

}
