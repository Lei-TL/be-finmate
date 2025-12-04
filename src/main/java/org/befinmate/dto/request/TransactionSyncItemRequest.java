package org.befinmate.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class TransactionSyncItemRequest {

    private String id;          // client có thể tự sinh hoặc null

    private Instant updatedAt;  // timestamp local phía client (để dành last-write-wins sau này)

    private boolean deleted;

    private String walletId;
    private String categoryId;
    private String type;        // INCOME / EXPENSE / TRANSFER
    private BigDecimal amount;
    private String currency;
    private Instant occurredAt;
    private String note;
    private String transferRefId;
}
