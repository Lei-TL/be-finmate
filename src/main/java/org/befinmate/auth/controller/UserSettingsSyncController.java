package org.befinmate.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.befinmate.dto.request.UserSettingsSyncRequest;
import org.befinmate.dto.response.UserSettingsSyncResponse;
import org.befinmate.auth.service.UserSettingsSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sync/settings")
@RequiredArgsConstructor
public class UserSettingsSyncController {

    private final UserSettingsSyncService userSettingsSyncService;

    private String getUserId(Jwt jwt) {
        return jwt.getSubject();
    }

    /**
     * GET /sync/settings/me
     */
    @GetMapping("/me")
    public ResponseEntity<UserSettingsSyncResponse> getMySettings(
            @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = getUserId(jwt);
        UserSettingsSyncResponse response =
                userSettingsSyncService.getSettingsForUser(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /sync/settings/me
     * Body: { "data": { ... } }
     */
    @PutMapping("/me")
    public ResponseEntity<UserSettingsSyncResponse> upsertMySettings(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UserSettingsSyncRequest request
    ) {
        String userId = getUserId(jwt);
        UserSettingsSyncResponse response =
                userSettingsSyncService.upsertSettingsForUser(userId, request);
        return ResponseEntity.ok(response);
    }
}
