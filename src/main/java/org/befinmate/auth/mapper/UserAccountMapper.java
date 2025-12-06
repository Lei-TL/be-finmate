package org.befinmate.auth.mapper;

import org.befinmate.auth.domain.UserAccount;
import org.befinmate.common.enums.Role;
import org.befinmate.entity.User;

public class UserAccountMapper {

    public static UserAccount toDomain(User entity) {
        if (entity == null) return null;

        return UserAccount.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .passwordHash(entity.getPassword())
                .role(entity.getRole())
                .active(entity.isActive())   // nếu User chưa có active thì tạm hard-code true
                .build();
    }

    public static User toEntity(UserAccount domain) {
        if (domain == null) return null;

        User user = new User();
        user.setId(domain.getId());
        user.setEmail(domain.getEmail());
        user.setPassword(domain.getPasswordHash());
        user.setRole(domain.getRole() != null ? domain.getRole() : Role.USER);
        user.setActive(domain.isActive());
        return user;
    }
}
