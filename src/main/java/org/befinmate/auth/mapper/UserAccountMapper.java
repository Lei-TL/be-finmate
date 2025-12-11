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
                .active(entity.isEnabled())   // map từ User.enabled sang UserAccount.active
                .build();
    }

    public static User toEntity(UserAccount domain) {
        if (domain == null) return null;

        User user = new User();
        user.setId(domain.getId());
        user.setEmail(domain.getEmail());
        user.setPassword(domain.getPasswordHash());
        user.setRole(domain.getRole() != null ? domain.getRole() : Role.USER);
        user.setEnabled(domain.isActive());  // map từ UserAccount.active sang User.enabled
        return user;
    }
}
