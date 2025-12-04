package org.befinmate.transaction;

import org.befinmate.dto.request.TransactionRequest;
import org.befinmate.dto.request.TransactionSyncRequest;
import org.befinmate.dto.response.TransactionResponse;
import org.befinmate.dto.response.TransactionSyncResponse;
import org.springframework.data.domain.Page;

import java.time.Instant;

public interface TransactionService {

    // CRUD + filter + pagination
    Page<TransactionResponse> getTransactions(
            String userId,
            Instant from,
            Instant to,
            String walletId,
            String categoryId,
            String type,
            int page,
            int size
    );

    TransactionResponse getTransactionById(String userId, String transactionId);

    TransactionResponse createTransaction(String userId, TransactionRequest request);

    TransactionResponse updateTransaction(String userId, String transactionId, TransactionRequest request);

    void deleteTransaction(String userId, String transactionId);

    // Sync
    TransactionSyncResponse syncPull(String userId, Instant since);

    TransactionSyncResponse syncPush(String userId, TransactionSyncRequest request);
}
