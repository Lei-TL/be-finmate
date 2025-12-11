package org.befinmate.auth.service.implement;

import lombok.RequiredArgsConstructor;
import org.befinmate.auth.domain.UserAccount;
import org.befinmate.auth.mapper.UserAccountMapper;
import org.befinmate.common.enums.Role;
import org.befinmate.dto.request.LoginRequest;
import org.befinmate.dto.request.RefreshTokenRequest;
import org.befinmate.dto.request.RegisterRequest;
import org.befinmate.dto.response.TokenResponse;
import org.befinmate.entity.User;
import org.befinmate.auth.repository.UserRepository;
import org.befinmate.auth.service.AuthService;
import org.befinmate.auth.service.TokenService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Override
    public TokenResponse login(LoginRequest request) {
        User userEntity = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        UserAccount user = UserAccountMapper.toDomain(userEntity);

        user.ensureActive();

        if (!passwordEncoder.matches(request.getPassword(), userEntity.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        userEntity.setUpdatedAt(Instant.now());
        userRepository.save(userEntity);

        return tokenService.generateTokenPair(userEntity);
    }

    @Override
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("Email already in use");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        userRepository.save(user);
    }

    @Override
    public TokenResponse refresh(RefreshTokenRequest request) {
        return tokenService.refreshToken(request);
    }

    @Override
    public void logout(String refreshToken) {
        tokenService.removeToken(refreshToken);
    }
}
