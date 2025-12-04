package org.befinmate.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String type; // CASH, BANK, ...

    @NotBlank
    private String currency; // VND, USD...

    @NotNull
    @PositiveOrZero
    private BigDecimal initialBalance;

    // có thể null -> giữ nguyên
    private Boolean archived;

    private String color;
}
