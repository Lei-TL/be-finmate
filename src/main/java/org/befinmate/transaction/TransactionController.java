package org.befinmate.transaction;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.befinmate.dto.request.TransactionRequest;
import org.befinmate.dto.request.TransactionSyncRequest;
import org.befinmate.dto.response.TransactionResponse;
import org.befinmate.dto.response.TransactionSyncResponse;
import org.befinmate.transaction.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    private String getUserId(Jwt jwt) {
        return jwt.getSubject();
    }

    // ====== CRUD + FILTER ======

    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> getTransactions(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) String walletId,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        String userId = getUserId(jwt);

        Page<TransactionResponse> result = transactionService.getTransactions(
                userId, from, to, walletId, categoryId, type, page, size
        );

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransactionById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id
    ) {
        String userId = getUserId(jwt);
        TransactionResponse response = transactionService.getTransactionById(userId, id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody TransactionRequest request
    ) {
        String userId = getUserId(jwt);
        TransactionResponse response = transactionService.createTransaction(userId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id,
            @Valid @RequestBody TransactionRequest request
    ) {
        String userId = getUserId(jwt);
        TransactionResponse response = transactionService.updateTransaction(userId, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id
    ) {
        String userId = getUserId(jwt);
        transactionService.deleteTransaction(userId, id);
        return ResponseEntity.noContent().build();
    }

    // ====== SYNC ======

    @GetMapping("/sync")
    public ResponseEntity<TransactionSyncResponse> syncPull(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant since
    ) {
        String userId = getUserId(jwt);
        TransactionSyncResponse response = transactionService.syncPull(userId, since);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/sync")
    public ResponseEntity<TransactionSyncResponse> syncPush(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody TransactionSyncRequest request
    ) {
        String userId = getUserId(jwt);
        TransactionSyncResponse response = transactionService.syncPush(userId, request);
        return ResponseEntity.ok(response);
    }
}
