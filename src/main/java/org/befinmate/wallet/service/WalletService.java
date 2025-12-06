package org.befinmate.wallet.service;

import org.befinmate.dto.request.WalletRequest;
import org.befinmate.dto.request.WalletSyncRequest;
import org.befinmate.dto.response.WalletResponse;
import org.befinmate.dto.response.WalletSyncResponse;

import java.time.Instant;
import java.util.List;

public interface WalletService {

    // CRUD

    List<WalletResponse> getMyWallets(String userId);

    WalletResponse getMyWalletById(String userId, String walletId);

    WalletResponse createWallet(String userId, WalletRequest request);

    WalletResponse updateWallet(String userId, String walletId, WalletRequest request);

    void deleteWallet(String userId, String walletId);

    // Sync

    WalletSyncResponse syncPull(String userId, Instant since);

    WalletSyncResponse syncPush(String userId, WalletSyncRequest request);
}
