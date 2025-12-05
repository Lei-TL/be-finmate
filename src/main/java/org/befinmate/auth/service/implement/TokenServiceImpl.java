package org.befinmate.auth.service.implement;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.befinmate.common.properties.JwtProperties;
import org.befinmate.dto.request.RefreshTokenRequest;
import org.befinmate.dto.response.TokenResponse;
import org.befinmate.entity.User;
import org.befinmate.auth.repository.UserRepository;
import org.befinmate.auth.service.TokenService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final UserRepository userRepository;

    @Qualifier("accessTokenEncoder")
    private final JwtEncoder accessTokenEncoder;

    @Qualifier("refreshTokenEncoder")
    private final JwtEncoder refreshTokenEncoder;

    @Qualifier("refreshTokenDecoder")
    private final JwtDecoder refreshTokenDecoder;

    private final JwtProperties jwtProperties;

    // ==========================
    //  GENERATE PAIR
    // ==========================
    @Override
    public TokenResponse generateTokenPair(User account) {
        String accessToken = generateAccessToken(account);
        String refreshToken = generateRefreshToken(account);
        return new TokenResponse(accessToken, refreshToken);
    }

    // ==========================
    //  ACCESS TOKEN
    // ==========================
    private String generateAccessToken(User account) {

        Instant now = Instant.now();
        Instant expiresAt = now.plus(
                jwtProperties.getAccessTokenExpirationMinutes(),
                ChronoUnit.MINUTES
        );

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(account.getId())
                .claim("email", account.getEmail())
                .claim("roles", List.of(account.getRole().name()))
                .claim("token_type", "access")
                .claim("jti", UUID.randomUUID().toString())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return accessTokenEncoder
                .encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();
    }

    // ==========================
    //  REFRESH TOKEN
    // ==========================
    private String generateRefreshToken(User account) {

        Instant now = Instant.now();
        Instant expiresAt = now.plus(
                jwtProperties.getRefreshTokenExpirationDays(),
                ChronoUnit.DAYS
        );

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(account.getId())
                .claim("email", account.getEmail())
                .claim("roles", List.of(account.getRole().name()))
                .claim("token_type", "refresh")
                .claim("jti", UUID.randomUUID().toString())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return refreshTokenEncoder
                .encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();
    }

    // ==========================
    //  REFRESH TOKEN FLOW
    // ==========================
    @Override
    @SneakyThrows
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        Jwt decoded = refreshTokenDecoder.decode(refreshToken);

        // 1) Check issuer
        if (!jwtProperties.getIssuer().equals(decoded.getIssuer())) {
            throw new JwtException("Invalid issuer");
        }

        // 2) Check token type
        String tokenType = decoded.getClaimAsString("token_type");
        if (!"refresh".equals(tokenType)) {
            throw new JwtException("Invalid token type");
        }

        // 3) Check subject
        String userId = decoded.getSubject();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 4) Generate new pair
        return generateTokenPair(user);
    }

    @Override
    public void removeToken(String token) {
        // TODO: implement blacklist / lưu refresh token nếu cần revoke
    }
}
