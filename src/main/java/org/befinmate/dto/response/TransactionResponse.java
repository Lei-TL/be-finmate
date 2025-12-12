package org.befinmate.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class TransactionResponse {

    private String id;
    private String walletId;
    private String categoryId;
    private String categoryName; // ✅ Thêm categoryName để frontend không cần lookup
    private String type;
    private BigDecimal amount;
    private String currency;
    private Instant occurredAt;
    private String note;
    private String transferRefId;
    private boolean deleted;
    private Instant createdAt;
    private Instant updatedAt;
}
