package org.befinmate.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.befinmate.auth.service.TokenService;
import org.befinmate.auth.repository.UserRepository;
import org.befinmate.common.enums.Role;
import org.befinmate.dto.request.LoginRequest;
import org.befinmate.dto.request.RefreshTokenRequest;
import org.befinmate.dto.request.RegisterRequest;
import org.befinmate.dto.response.TokenResponse;
import org.befinmate.dto.response.UserInfoResponse;
import org.befinmate.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final TokenService tokenService;

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@Valid @RequestBody RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // Lưu đầy đủ thông tin từ request
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER) // default
                .fullName(request.getFullName()) // Lưu đúng giá trị được gửi lên từ frontend
                .avatarUrl(request.getAvatarUrl()) // Lưu đúng giá trị được gửi lên từ frontend
                .enabled(true)
                .build();

        user = userRepository.save(user);

        TokenResponse tokens = tokenService.generateTokenPair(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(tokens);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

            TokenResponse tokens = tokenService.generateTokenPair(user);

            return ResponseEntity.ok(tokens);

        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse tokens = tokenService.refreshToken(request);
        return ResponseEntity.ok(tokens);
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserInfoResponse response = UserInfoResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .build();
        
        return ResponseEntity.ok(response);
    }
}
