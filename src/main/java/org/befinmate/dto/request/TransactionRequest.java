package org.befinmate.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class TransactionRequest {

    @NotBlank
    private String walletId;

    // có thể null nếu là TRANSFER và anh handle riêng
    private String categoryId;

    // INCOME / EXPENSE / TRANSFER
    @NotBlank
    private String type;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotBlank
    private String currency;

    @NotNull
    private Instant occurredAt;

    private String note;

    private String transferRefId; // optional
}
