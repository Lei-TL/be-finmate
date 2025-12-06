package org.befinmate.wallet.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.befinmate.dto.request.WalletRequest;
import org.befinmate.dto.request.WalletSyncRequest;
import org.befinmate.dto.response.WalletResponse;
import org.befinmate.dto.response.WalletSyncResponse;
import org.befinmate.wallet.service.WalletService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    private String getUserId(Jwt jwt) {
        return jwt.getSubject();
    }

    // ========= CRUD =========

    @GetMapping
    public ResponseEntity<List<WalletResponse>> getMyWallets(@AuthenticationPrincipal Jwt jwt) {
        String userId = getUserId(jwt);
        return ResponseEntity.ok(walletService.getMyWallets(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WalletResponse> getWalletById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id
    ) {
        String userId = getUserId(jwt);
        return ResponseEntity.ok(walletService.getMyWalletById(userId, id));
    }

    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody WalletRequest request
    ) {
        String userId = getUserId(jwt);
        WalletResponse response = walletService.createWallet(userId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WalletResponse> updateWallet(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id,
            @Valid @RequestBody WalletRequest request
    ) {
        String userId = getUserId(jwt);
        WalletResponse response = walletService.updateWallet(userId, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWallet(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id
    ) {
        String userId = getUserId(jwt);
        walletService.deleteWallet(userId, id);
        return ResponseEntity.noContent().build();
    }

    // ========= SYNC =========

    @GetMapping("/sync")
    public ResponseEntity<WalletSyncResponse> syncPull(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant since
    ) {
        String userId = getUserId(jwt);
        WalletSyncResponse response = walletService.syncPull(userId, since);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/sync")
    public ResponseEntity<WalletSyncResponse> syncPush(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody WalletSyncRequest request
    ) {
        String userId = getUserId(jwt);
        WalletSyncResponse response = walletService.syncPush(userId, request);
        return ResponseEntity.ok(response);
    }
}
