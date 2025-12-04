package org.befinmate.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TransactionSyncResponse {
    private List<TransactionResponse> items;
}
