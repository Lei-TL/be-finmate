package org.befinmate.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "user_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSettings {

    @Id
    @Column(name = "user_id")
    private String userId;

    // Nếu anh muốn map quan hệ 1-1 với User:
    // Có thể dùng @OneToOne + @MapsId, nếu thấy phức tạp thì bỏ block này đi cũng được.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    /**
     * JSON cấu hình của user, ví dụ:
     * {
     *   "fullName": "Nguyen Van A",
     *   "defaultCurrency": "VND",
     *   "language": "vi",
     *   "timeZone": "Asia/Ho_Chi_Minh",
     *   "theme": "dark"
     * }
     */
    @Lob
    @Column(name = "settings_json", columnDefinition = "TEXT")
    private String settingsJson;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
