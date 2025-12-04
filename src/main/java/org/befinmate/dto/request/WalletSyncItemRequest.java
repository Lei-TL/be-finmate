package org.befinmate.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Một item sync từ client -> server
 */
@Data
public class WalletSyncItemRequest {

    private String id;          // client có thể tự generate, hoặc null nếu muốn để server sinh

    private Instant updatedAt;  // timestamp local của client

    private boolean deleted;    // nếu true => server đánh dấu deleted

    private String name;
    private String type;
    private String currency;
    private BigDecimal initialBalance;
    private Boolean archived;
    private String color;
}
