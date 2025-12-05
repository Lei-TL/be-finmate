package org.befinmate.entity;

import jakarta.persistence.*;
import lombok.*;
import org.befinmate.common.enums.TransactionType;

@Entity
@Table(
        name = "categories",
        indexes = {
                @Index(name = "idx_categories_user", columnList = "user_id"),
                @Index(name = "idx_categories_user_type", columnList = "user_id, type")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends BaseEntity {

    // null -> global category áp dụng cho mọi user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    // INCOME / EXPENSE / TRANSFER
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private TransactionType type;

    @Column(name = "icon", length = 100)
    private String icon;

    @Column(name = "display_order")
    private Integer displayOrder;
}
