package org.befinmate.entity;

import jakarta.persistence.*;
import lombok.*;
import org.befinmate.common.enums.FriendStatus;

@Entity
@Table(
        name = "friendships",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_friendships_user_pair",
                        columnNames = {"from_user_id", "to_user_id"}
                )
        },
        indexes = {
                @Index(name = "idx_friendships_from_user", columnList = "from_user_id"),
                @Index(name = "idx_friendships_to_user", columnList = "to_user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Friendship extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_user_id", nullable = false)
    private User fromUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_user_id", nullable = false)
    private User toUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FriendStatus status;
}
