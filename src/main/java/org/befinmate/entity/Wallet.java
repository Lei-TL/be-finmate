package org.befinmate.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "wallets",
        indexes = {
                @Index(name = "idx_wallets_user", columnList = "user_id"),
                @Index(name = "idx_wallets_user_default", columnList = "user_id, is_default")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "type", length = 50)
    private String type;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "initial_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal initialBalance = BigDecimal.ZERO;

    @Column(name = "current_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    @Column(name = "archived", nullable = false)
    private boolean archived = false;

    @Column(name = "color", length = 20)
    private String color; // Mã màu hex nếu muốn
}
