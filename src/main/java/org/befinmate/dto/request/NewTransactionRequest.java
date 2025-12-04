package org.befinmate.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NewTransactionRequest {

    private long amount;

}
