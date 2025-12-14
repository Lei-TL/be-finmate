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

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
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

        Jwt decoded;
        try {
            decoded = refreshTokenDecoder.decode(refreshToken);
        } catch (IllegalArgumentException | JwtException e) {
            // If decode fails due to issuer URL conversion (old token with "befinmate" issuer)
            // The error message should contain "iss" or "issuer"
            if (e.getMessage() != null && 
                (e.getMessage().contains("iss") || e.getMessage().contains("issuer") || 
                 e.getMessage().contains("Unable to convert claim"))) {
                // Try to decode manually without issuer URL validation
                decoded = decodeTokenManually(refreshToken);
            } else {
                throw new JwtException("Invalid refresh token: " + e.getMessage(), e);
            }
        }

        // 1) Check issuer - support both old ("befinmate") and new (URL) issuer for backward compatibility
        String issuer = decoded.getClaimAsString("iss");
        String expectedIssuer = jwtProperties.getIssuer();
        
        // Allow both old issuer "befinmate" and new issuer (URL) for backward compatibility
        boolean isValidIssuer = issuer != null && 
                (expectedIssuer.equals(issuer) || "befinmate".equals(issuer));
        
        if (!isValidIssuer) {
            throw new JwtException("Invalid issuer: " + issuer);
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
    
    /**
     * Manually decode JWT token without issuer URL validation (for backward compatibility)
     * This handles old tokens with "befinmate" issuer that can't be converted to URL
     */
    private Jwt decodeTokenManually(String token) {
        try {
            // Parse JWT manually to extract claims without issuer URL validation
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new JwtException("Invalid token format");
            }
            
            // Decode payload (base64url)
            String payloadJson = new String(
                    java.util.Base64.getUrlDecoder().decode(parts[1]), 
                    java.nio.charset.StandardCharsets.UTF_8
            );
            
            // Parse JSON to get claims using Jackson
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> claims = mapper.readValue(payloadJson, 
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            
            // Verify signature using HMAC
            javax.crypto.SecretKey key = new javax.crypto.spec.SecretKeySpec(
                    jwtProperties.getRefreshSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(key);
            String signature = java.util.Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal((parts[0] + "." + parts[1]).getBytes()));
            
            if (!signature.equals(parts[2])) {
                throw new JwtException("Invalid token signature");
            }
            
            // Build Jwt object from claims
            return Jwt.withTokenValue(token)
                    .header("alg", "HS256")
                    .claims(c -> c.putAll(claims))
                    .build();
        } catch (Exception e) {
            throw new JwtException("Failed to decode token: " + e.getMessage(), e);
        }
    }
}
