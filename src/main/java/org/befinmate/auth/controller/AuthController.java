package org.befinmate.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.befinmate.auth.service.TokenService;
import org.befinmate.auth.repository.UserRepository;
import org.befinmate.common.enums.Role;
import org.befinmate.dto.request.LoginRequest;
import org.befinmate.dto.request.RefreshTokenRequest;
import org.befinmate.dto.request.RegisterRequest;
import org.befinmate.dto.request.UpdateUserRequest;
import org.befinmate.dto.request.WalletRequest;
import org.befinmate.dto.response.TokenResponse;
import org.befinmate.dto.response.UserInfoResponse;
import org.befinmate.entity.User;
import org.befinmate.wallet.service.WalletService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final WalletService walletService;

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@Valid @RequestBody RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // Parse birthday từ String (yyyy-MM-dd) sang LocalDate
        LocalDate birthday = null;
        if (request.getBirthday() != null && !request.getBirthday().isEmpty()) {
            try {
                birthday = LocalDate.parse(request.getBirthday(), DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException e) {
                // Nếu parse fail, giữ null (mặc định)
                birthday = null;
            }
        }

        // Lưu đầy đủ thông tin từ request
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER) // default
                .fullName(request.getFullName()) // Lưu đúng giá trị được gửi lên từ frontend
                .avatarUrl(request.getAvatarUrl()) // Lưu đúng giá trị được gửi lên từ frontend
                .birthday(birthday) // Lưu birthday (có thể null)
                .enabled(true)
                .build();

        user = userRepository.save(user);

        // ✅ Tạo ví chính (Main Wallet) cho user mới đăng ký
        try {
            WalletRequest defaultWalletRequest = new WalletRequest();
            defaultWalletRequest.setName("Ví chính");
            defaultWalletRequest.setType("CASH");
            defaultWalletRequest.setCurrency("VND");
            defaultWalletRequest.setInitialBalance(BigDecimal.ZERO);
            defaultWalletRequest.setArchived(false);
            defaultWalletRequest.setColor(null);
            
            walletService.createWallet(user.getId(), defaultWalletRequest);
            System.out.println("✅ Default wallet created successfully for user: " + user.getId());
        } catch (Exception e) {
            // ✅ Log error nhưng không fail registration nếu tạo ví thất bại
            System.err.println("❌ Failed to create default wallet for user " + user.getId() + ": " + e.getMessage());
            e.printStackTrace();
        }

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
        
        // Format birthday từ LocalDate sang String (yyyy-MM-dd) hoặc null
        String birthdayStr = null;
        if (user.getBirthday() != null) {
            birthdayStr = user.getBirthday().format(DateTimeFormatter.ISO_LOCAL_DATE);
        }

        UserInfoResponse response = UserInfoResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .birthday(birthdayStr) // Format: yyyy-MM-dd hoặc null
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * ✅ Update thông tin user (fullName, avatarUrl, birthday)
     */
    @PutMapping("/me")
    public ResponseEntity<UserInfoResponse> updateCurrentUser(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateUserRequest request) {
        String userId = jwt.getSubject();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ✅ Update fullName nếu có
        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            user.setFullName(request.getFullName().trim());
        }

        // ✅ Update avatarUrl nếu có
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        // ✅ Update birthday nếu có
        if (request.getBirthday() != null && !request.getBirthday().isEmpty()) {
            try {
                LocalDate birthday = LocalDate.parse(request.getBirthday(), DateTimeFormatter.ISO_LOCAL_DATE);
                user.setBirthday(birthday);
            } catch (DateTimeParseException e) {
                // Nếu parse fail, giữ nguyên birthday cũ
                System.err.println("Invalid birthday format: " + request.getBirthday());
            }
        }

        user = userRepository.save(user);

        // ✅ Format response
        String birthdayStr = null;
        if (user.getBirthday() != null) {
            birthdayStr = user.getBirthday().format(DateTimeFormatter.ISO_LOCAL_DATE);
        }

        UserInfoResponse response = UserInfoResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .birthday(birthdayStr)
                .build();

        return ResponseEntity.ok(response);
    }
}
