package org.befinmate.auth;

import org.befinmate.dto.request.LoginRequest;
import org.befinmate.dto.request.RefreshTokenRequest;
import org.befinmate.dto.request.RegisterRequest;
import org.befinmate.dto.response.TokenResponse;

public interface AuthService {
    TokenResponse login(LoginRequest request);
    void register(RegisterRequest request);
    TokenResponse refresh(RefreshTokenRequest request);
    void logout(String refreshToken);
}
