package org.befinmate.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class TransactionSyncItemRequest {

    private String id;

    private Instant updatedAt;

    private boolean deleted;

    private String walletId;
    private String categoryId;
    private String type;
    private BigDecimal amount;
    private String currency;
    private Instant occurredAt;
    private String note;
    private String transferRefId;
}
