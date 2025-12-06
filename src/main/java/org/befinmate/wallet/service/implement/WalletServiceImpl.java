package org.befinmate.wallet.service.implement;

import lombok.RequiredArgsConstructor;
import org.befinmate.dto.request.WalletRequest;
import org.befinmate.dto.request.WalletSyncItemRequest;
import org.befinmate.dto.request.WalletSyncRequest;
import org.befinmate.dto.response.WalletResponse;
import org.befinmate.dto.response.WalletSyncResponse;
import org.befinmate.entity.User;
import org.befinmate.entity.Wallet;
import org.befinmate.auth.repository.UserRepository;
import org.befinmate.wallet.repository.WalletRepository;
import org.befinmate.wallet.service.WalletService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    private User getUserOrThrow(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private WalletResponse toResponse(Wallet w) {
        return WalletResponse.builder()
                .id(w.getId())
                .name(w.getName())
                .type(w.getType())
                .currency(w.getCurrency())
                .initialBalance(w.getInitialBalance())
                .archived(w.isArchived())
                .deleted(w.isDeleted())
                .color(w.getColor())
                .createdAt(w.getCreatedAt())
                .updatedAt(w.getUpdatedAt())
                .build();
    }

    // =======================
    // CRUD (online-first)
    // =======================

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "wallets", key = "#userId")
    public List<WalletResponse> getMyWallets(String userId) {
        return walletRepository.findByUserIdAndDeletedFalseOrderByCreatedAtAsc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponse getMyWalletById(String userId, String walletId) {
        Wallet wallet = walletRepository.findByIdAndUserId(walletId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));
        if (wallet.isDeleted()) {
            throw new IllegalArgumentException("Wallet has been deleted");
        }
        return toResponse(wallet);
    }

    @Override
    @CacheEvict(cacheNames = "wallets", key = "#userId")
    public WalletResponse createWallet(String userId, WalletRequest request) {
        User user = getUserOrThrow(userId);

        Wallet wallet = Wallet.builder()
                .user(user)
                .name(request.getName())
                .type(request.getType())
                .currency(request.getCurrency())
                .initialBalance(request.getInitialBalance())
                .archived(Boolean.TRUE.equals(request.getArchived()))
                .deleted(false)
                .color(request.getColor())
                .build();

        Wallet saved = walletRepository.save(wallet);
        return toResponse(saved);
    }

    @Override
    @CacheEvict(cacheNames = "wallets", key = "#userId")
    public WalletResponse updateWallet(String userId, String walletId, WalletRequest request) {
        Wallet wallet = walletRepository.findByIdAndUserId(walletId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        if (wallet.isDeleted()) {
            throw new IllegalArgumentException("Wallet has been deleted");
        }

        wallet.setName(request.getName());
        wallet.setType(request.getType());
        wallet.setCurrency(request.getCurrency());
        wallet.setInitialBalance(request.getInitialBalance());
        wallet.setArchived(Boolean.TRUE.equals(request.getArchived()));
        wallet.setColor(request.getColor());

        Wallet saved = walletRepository.save(wallet);
        return toResponse(saved);
    }

    @Override
    @CacheEvict(cacheNames = "wallets", key = "#userId")
    public void deleteWallet(String userId, String walletId) {
        Wallet wallet = walletRepository.findByIdAndUserId(walletId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        // Soft delete
        wallet.setDeleted(true);
        wallet.setArchived(true);

        walletRepository.save(wallet);
    }

    // =======================
    // SYNC (offline / incremental)
    // =======================

    @Override
    @Transactional(readOnly = true)
    public WalletSyncResponse syncPull(String userId, Instant since) {
        List<Wallet> wallets;

        if (since == null) {
            // lần đầu: lấy tất cả (kể cả deleted=false/true? tuỳ anh)
            wallets = walletRepository.findByUserIdAndDeletedFalseOrderByCreatedAtAsc(userId);
        } else {
            // incremental: lấy mọi thay đổi từ mốc đó (bao gồm cả deleted)
            wallets = walletRepository.findByUserIdAndUpdatedAtAfter(userId, since);
        }

        List<WalletResponse> items = wallets.stream()
                .sorted(Comparator.comparing(Wallet::getUpdatedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toResponse)
                .toList();

        return WalletSyncResponse.builder()
                .items(items)
                .build();
    }

    @Override
    @CacheEvict(cacheNames = "wallets", key = "#userId")
    public WalletSyncResponse syncPush(String userId, WalletSyncRequest request) {

        User user = getUserOrThrow(userId);

        if (request.getItems() == null || request.getItems().isEmpty()) {
            return WalletSyncResponse.builder()
                    .items(List.of())
                    .build();
        }

        for (WalletSyncItemRequest item : request.getItems()) {

            Wallet wallet = null;

            if (item.getId() != null) {
                wallet = walletRepository.findByIdAndUserId(item.getId(), userId).orElse(null);
            }

            // nếu chưa có -> tạo mới
            if (wallet == null) {
                wallet = new Wallet();
                wallet.setId(item.getId() != null ? item.getId() : UUID.randomUUID().toString());
                wallet.setUser(user);
                // default
                wallet.setInitialBalance(BigDecimal.ZERO);
            }

            // last-write-wins đơn giản:
            // nếu muốn cứng hơn, có thể so sánh item.updatedAt với wallet.updatedAt
            wallet.setName(item.getName() != null ? item.getName() : wallet.getName());
            wallet.setType(item.getType() != null ? item.getType() : wallet.getType());
            wallet.setCurrency(item.getCurrency() != null ? item.getCurrency() : wallet.getCurrency());
            if (item.getInitialBalance() != null) {
                wallet.setInitialBalance(item.getInitialBalance());
            }
            if (item.getArchived() != null) {
                wallet.setArchived(item.getArchived());
            }
            wallet.setColor(item.getColor() != null ? item.getColor() : wallet.getColor());
            wallet.setDeleted(item.isDeleted());

            walletRepository.save(wallet);
        }

        // Trả lại list wallet hiện tại cho client (anh có thể tối ưu chỉ trả những cái vừa sync)
        List<Wallet> wallets = walletRepository.findByUserIdAndDeletedFalseOrderByCreatedAtAsc(userId);
        List<WalletResponse> responses = wallets.stream()
                .map(this::toResponse)
                .toList();

        return WalletSyncResponse.builder()
                .items(responses)
                .build();
    }
}
