package org.befinmate.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "user_settings",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_settings_user_id", columnNames = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSettings extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @Lob
    @Column(name = "settings_json", columnDefinition = "TEXT", nullable = false)
    private String settingsJson;
}
