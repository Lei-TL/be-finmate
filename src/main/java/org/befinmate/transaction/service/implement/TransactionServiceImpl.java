package org.befinmate.transaction.service.implement;

import lombok.RequiredArgsConstructor;
import org.befinmate.auth.repository.UserRepository;
import org.befinmate.category.repository.CategoryRepository;
import org.befinmate.dto.request.TransactionRequest;
import org.befinmate.dto.request.TransactionSyncItemRequest;
import org.befinmate.dto.request.TransactionSyncRequest;
import org.befinmate.dto.response.TransactionResponse;
import org.befinmate.dto.response.TransactionSyncResponse;
import org.befinmate.common.enums.TransactionType;
import org.befinmate.entity.*;
import org.befinmate.transaction.repository.TransactionRepository;
import org.befinmate.transaction.service.TransactionService;
import org.befinmate.wallet.repository.WalletRepository;
import org.springframework.data.domain.*;
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
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final CategoryRepository categoryRepository;

    private User getUserOrThrow(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private Wallet getWalletOrThrow(String userId, String walletId) {
        return walletRepository.findByIdAndUserId(walletId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found or not owned by user"));
    }

    private Category getCategoryIfValid(String userId, String categoryId) {
        if (categoryId == null) return null;
        // ✅ Categories giờ là global, không cần check userId
        return categoryRepository.findById(categoryId)
                .filter(c -> !c.isDeleted()) // Chỉ lấy category chưa bị xóa
                .orElseThrow(() -> new IllegalArgumentException("Category not found or not accessible"));
    }

    private TransactionResponse toResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .walletId(t.getWallet() != null ? t.getWallet().getId() : null)
                .categoryId(t.getCategory() != null ? t.getCategory().getId() : null)
                .categoryName(t.getCategory() != null ? t.getCategory().getName() : null) // ✅ Thêm categoryName
                .type(t.getType() != null ? t.getType().name() : null)
                .amount(t.getAmount())
                .currency(t.getCurrency())
                .occurredAt(t.getOccurredAt())
                .note(t.getNote())
                .transferRefId(t.getTransferRefId())
                .deleted(t.isDeleted())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }

    // ============= CRUD + FILTER =============

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactions(
            String userId,
            Instant from,
            Instant to,
            String walletId,
            String categoryId,
            String type,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.max(size, 1)
        );

        Page<Transaction> result = transactionRepository.findByUserWithFilters(
                userId, from, to, walletId, categoryId, type, pageable
        );

        List<TransactionResponse> mapped = result
                .stream()
                .map(this::toResponse)
                .toList();

        return new PageImpl<>(mapped, pageable, result.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(String userId, String transactionId) {
        Transaction tx = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        if (tx.isDeleted()) {
            throw new IllegalArgumentException("Transaction has been deleted");
        }

        return toResponse(tx);
    }

    @Override
    public TransactionResponse createTransaction(String userId, TransactionRequest request) {
        User user = getUserOrThrow(userId);
        Wallet wallet = getWalletOrThrow(userId, request.getWalletId());
        Category category = getCategoryIfValid(userId, request.getCategoryId());

        Transaction tx = Transaction.builder()
                .user(user)
                .wallet(wallet)
                .category(category)
                .type(TransactionType.valueOf(request.getType()))
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .occurredAt(request.getOccurredAt())
                .note(request.getNote())
                .transferRefId(request.getTransferRefId())
                .build();
        tx.setDeleted(false);

        Transaction saved = transactionRepository.save(tx);
        return toResponse(saved);
    }

    @Override
    public TransactionResponse updateTransaction(String userId, String transactionId, TransactionRequest request) {
        Transaction tx = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        if (tx.isDeleted()) {
            throw new IllegalArgumentException("Transaction has been deleted");
        }

        Wallet wallet = getWalletOrThrow(userId, request.getWalletId());
        Category category = getCategoryIfValid(userId, request.getCategoryId());

        tx.setWallet(wallet);
        tx.setCategory(category);
        tx.setType(TransactionType.valueOf(request.getType()));
        tx.setAmount(request.getAmount());
        tx.setCurrency(request.getCurrency());
        tx.setOccurredAt(request.getOccurredAt());
        tx.setNote(request.getNote());
        tx.setTransferRefId(request.getTransferRefId());

        Transaction saved = transactionRepository.save(tx);
        return toResponse(saved);
    }

    @Override
    public void deleteTransaction(String userId, String transactionId) {
        Transaction tx = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        tx.setDeleted(true);
        transactionRepository.save(tx);
    }

    // ============= SYNC =============

    @Override
    @Transactional(readOnly = true)
    public TransactionSyncResponse syncPull(String userId, Instant since) {
        List<Transaction> transactions;

        if (since == null) {
            // Lần đầu: gửi tất cả transaction chưa deleted (cho đơn giản)
            Page<Transaction> page = transactionRepository.findByUserWithFilters(
                    userId, null, null, null, null, null,
                    PageRequest.of(0, Integer.MAX_VALUE)
            );
            transactions = page.getContent();
        } else {
            // Incremental: mọi transaction (kể cả deleted) updated sau mốc since
            transactions = transactionRepository.findByUserIdAndUpdatedAtAfter(userId, since);
        }

        List<TransactionResponse> items = transactions.stream()
                .sorted(Comparator.comparing(Transaction::getUpdatedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toResponse)
                .toList();

        return TransactionSyncResponse.builder()
                .items(items)
                .build();
    }

    @Override
    public TransactionSyncResponse syncPush(String userId, TransactionSyncRequest request) {

        User user = getUserOrThrow(userId);

        if (request.getItems() == null || request.getItems().isEmpty()) {
            return TransactionSyncResponse.builder()
                    .items(List.of())
                    .build();
        }

        for (TransactionSyncItemRequest item : request.getItems()) {

            Transaction tx = null;

            if (item.getId() != null) {
                tx = transactionRepository.findByIdAndUserId(item.getId(), userId).orElse(null);
            }

            if (tx == null) {
                // Kiểm tra các trường bắt buộc khi tạo mới
                if (item.getWalletId() == null || item.getType() == null || 
                    item.getCurrency() == null || item.getOccurredAt() == null) {
                    continue; // Bỏ qua nếu thiếu trường bắt buộc khi tạo mới
                }
                tx = new Transaction();
                tx.setId(item.getId() != null ? item.getId() : UUID.randomUUID().toString());
                tx.setUser(user);
                tx.setAmount(item.getAmount() != null ? item.getAmount() : BigDecimal.ZERO);
            }

            // Wallet & Category check
            if (item.getWalletId() != null) {
                Wallet wallet = getWalletOrThrow(userId, item.getWalletId());
                tx.setWallet(wallet);
            } else if (tx.getWallet() == null) {
                continue; // Bỏ qua nếu không có wallet (required field)
            }

            if (item.getCategoryId() != null) {
                Category category = getCategoryIfValid(userId, item.getCategoryId());
                tx.setCategory(category);
            }

            // Last-write-wins đơn giản: luôn ghi đè nếu có giá trị mới
            if (item.getType() != null) {
                try {
                    tx.setType(TransactionType.valueOf(item.getType()));
                } catch (IllegalArgumentException e) {
                    continue; // Bỏ qua nếu type không hợp lệ
                }
            } else if (tx.getType() == null) {
                continue; // Bỏ qua nếu không có type (required field)
            }
            if (item.getAmount() != null) {
                tx.setAmount(item.getAmount());
            }
            if (item.getCurrency() != null) {
                tx.setCurrency(item.getCurrency());
            } else if (tx.getCurrency() == null) {
                continue; // Bỏ qua nếu không có currency (required field)
            }
            if (item.getOccurredAt() != null) {
                tx.setOccurredAt(item.getOccurredAt());
            } else if (tx.getOccurredAt() == null) {
                continue; // Bỏ qua nếu không có occurredAt (required field)
            }
            if (item.getNote() != null) {
                tx.setNote(item.getNote());
            }
            if (item.getTransferRefId() != null) {
                tx.setTransferRefId(item.getTransferRefId());
            }

            tx.setDeleted(item.isDeleted());

            transactionRepository.save(tx);
        }

        // Sau sync trả lại danh sách hiện tại (non-deleted) cho client (tuỳ anh muốn tối ưu)
        Page<Transaction> page = transactionRepository.findByUserWithFilters(
                userId, null, null, null, null, null,
                PageRequest.of(0, Integer.MAX_VALUE)
        );

        List<TransactionResponse> responses = page.stream()
                .map(this::toResponse)
                .toList();

        return TransactionSyncResponse.builder()
                .items(responses)
                .build();
    }
}
