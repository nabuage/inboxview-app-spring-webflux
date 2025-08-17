package org.inboxview.app.user.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserMailboxTransaction {
    private Long transactionId;
    private String merchantName;
    private LocalDate transactionDate;
    private BigDecimal amount;
}
