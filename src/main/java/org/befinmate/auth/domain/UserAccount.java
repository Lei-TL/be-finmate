package org.befinmate.auth.domain;

import lombok.Builder;
import lombok.Getter;
import org.befinmate.common.enums.Role;

@Getter
@Builder
public class UserAccount {

    private final String id;
    private final String email;
    private final String passwordHash;
    private final Role role;
    private final boolean active;

    // Ví dụ rule: user phải active mới được login
    public void ensureActive() {
        if (!active) {
            throw new IllegalStateException("Tài khoản đã bị khóa hoặc không hoạt động");
        }
    }
}
