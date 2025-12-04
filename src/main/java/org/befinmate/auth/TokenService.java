package org.befinmate.auth;

import org.befinmate.dto.request.RefreshTokenRequest;
import org.befinmate.dto.response.TokenResponse;
import org.befinmate.entity.User;

public interface TokenService {

    TokenResponse generateTokenPair(User account);

    TokenResponse refreshToken(RefreshTokenRequest request);

    void removeToken(String token);
}
