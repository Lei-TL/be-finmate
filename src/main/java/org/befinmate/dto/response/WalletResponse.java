package org.befinmate.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class WalletResponse {

    private String id;
    private String name;
    private String type;
    private String currency;
    private BigDecimal initialBalance;
    private boolean archived;
    private boolean deleted;
    private String color;
    private Instant createdAt;
    private Instant updatedAt;
}
