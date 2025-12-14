package org.befinmate.entity;

import jakarta.persistence.*;
import lombok.*;
import org.befinmate.common.enums.TransactionType;

@Entity
@Table(
        name = "categories",
        indexes = {
                @Index(name = "idx_categories_type", columnList = "type")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends BaseEntity {

    // ✅ Categories là chung cho cả hệ thống, không gán user
    @Column(name = "name", nullable = false, length = 255, unique = true)
    private String name;

    // INCOME / EXPENSE / TRANSFER
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private TransactionType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @Column(name = "icon", length = 100)
    private String icon;

    @Column(name = "display_order")
    private Integer displayOrder;
}
