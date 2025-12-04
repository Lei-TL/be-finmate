package org.befinmate.auth.implement;

import lombok.RequiredArgsConstructor;
import org.befinmate.common.enums.Role;
import org.befinmate.dto.request.LoginRequest;
import org.befinmate.dto.request.RefreshTokenRequest;
import org.befinmate.dto.request.RegisterRequest;
import org.befinmate.dto.response.TokenResponse;
import org.befinmate.entity.User;
import org.befinmate.auth.UserRepository;
import org.befinmate.auth.AuthService;
import org.befinmate.auth.TokenService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Override
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        user.setUpdatedAt(Timestamp.from(Instant.now()));
        userRepository.save(user);

        return tokenService.generateTokenPair(user);
    }

    @Override
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("Email already in use");
        }

        Timestamp now = Timestamp.from(Instant.now());

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .createdAt(now)
                .updatedAt(now)
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
