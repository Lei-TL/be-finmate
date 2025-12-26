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
        
        // ✅ Update wallet balance sau khi tạo transaction
        updateWalletBalance(wallet.getId());
        
        return toResponse(saved);
    }
    
    /**
     * ✅ Tính lại và update wallet balance dựa trên tất cả transactions của wallet
     */
    private void updateWalletBalance(String walletId) {
        try {
            Wallet wallet = walletRepository.findById(walletId).orElse(null);
            if (wallet == null) {
                return;
            }
            
            // Lấy tất cả transactions của wallet này (chưa bị xóa)
            List<Transaction> transactions = transactionRepository.findByWalletIdAndDeletedFalse(walletId);
            
            // Tính lại balance từ initialBalance + tất cả transactions
            BigDecimal newBalance = wallet.getInitialBalance();
            
            if (transactions != null) {
                for (Transaction tx : transactions) {
                    if (tx.getType() != null && tx.getAmount() != null) {
                        if (tx.getType() == TransactionType.INCOME) {
                            newBalance = newBalance.add(tx.getAmount());
                        } else if (tx.getType() == TransactionType.EXPENSE) {
                            newBalance = newBalance.subtract(tx.getAmount());
                        }
                    }
                }
            }
            
            // Update wallet balance
            wallet.setCurrentBalance(newBalance);
            walletRepository.save(wallet);
        } catch (Exception e) {
            // Log error nhưng không throw để không rollback transaction
            System.err.println("Error updating wallet balance: " + e.getMessage());
            e.printStackTrace();
        }
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

        // ✅ Lưu wallet cũ để update balance sau
        Wallet oldWallet = tx.getWallet();
        
        tx.setWallet(wallet);
        tx.setCategory(category);
        tx.setType(TransactionType.valueOf(request.getType()));
        tx.setAmount(request.getAmount());
        tx.setCurrency(request.getCurrency());
        tx.setOccurredAt(request.getOccurredAt());
        tx.setNote(request.getNote());
        tx.setTransferRefId(request.getTransferRefId());

        Transaction saved = transactionRepository.save(tx);
        
        // ✅ Update wallet balance cho wallet mới
        updateWalletBalance(wallet.getId());
        
        // ✅ Nếu wallet thay đổi, update balance cho wallet cũ
        if (oldWallet != null && !oldWallet.getId().equals(wallet.getId())) {
            updateWalletBalance(oldWallet.getId());
        }
        
        return toResponse(saved);
    }

    @Override
    public void deleteTransaction(String userId, String transactionId) {
        Transaction tx = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        // ✅ Lưu walletId trước khi delete để update balance
        String walletId = tx.getWallet() != null ? tx.getWallet().getId() : null;

        tx.setDeleted(true);
        transactionRepository.save(tx);
        
        // ✅ Update wallet balance sau khi delete transaction
        if (walletId != null) {
            updateWalletBalance(walletId);
        }
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
            try {
                Transaction tx = null;

                if (item.getId() != null) {
                    tx = transactionRepository.findByIdAndUserId(item.getId(), userId).orElse(null);
                }

                if (tx == null) {
                    // Kiểm tra các trường bắt buộc khi tạo mới
                    if (item.getWalletId() == null || item.getType() == null || 
                        item.getCurrency() == null || item.getOccurredAt() == null) {
                        // Log và bỏ qua nếu thiếu trường bắt buộc khi tạo mới
                        System.err.println("Skipping transaction sync: missing required fields. walletId=" + 
                            item.getWalletId() + ", type=" + item.getType() + 
                            ", currency=" + item.getCurrency() + ", occurredAt=" + item.getOccurredAt());
                        continue;
                    }
                    tx = new Transaction();
                    tx.setId(item.getId() != null ? item.getId() : UUID.randomUUID().toString());
                    tx.setUser(user);
                    tx.setAmount(item.getAmount() != null ? item.getAmount() : BigDecimal.ZERO);
                    tx.setDeleted(false); // ✅ Set deleted = false khi tạo mới
                }

                // ✅ Lưu wallet cũ để update balance sau (nếu là update transaction)
                Wallet oldWallet = tx.getWallet();
                
                // Wallet & Category check
                if (item.getWalletId() != null) {
                    try {
                        Wallet wallet = getWalletOrThrow(userId, item.getWalletId());
                        tx.setWallet(wallet);
                    } catch (IllegalArgumentException e) {
                        // Wallet không tồn tại hoặc không thuộc user
                        System.err.println("Skipping transaction sync: wallet not found. walletId=" + item.getWalletId() + ", error=" + e.getMessage());
                        continue;
                    }
                } else if (tx.getWallet() == null) {
                    System.err.println("Skipping transaction sync: wallet is required but not provided");
                    continue; // Bỏ qua nếu không có wallet (required field)
                }

                if (item.getCategoryId() != null) {
                    try {
                        Category category = getCategoryIfValid(userId, item.getCategoryId());
                        tx.setCategory(category);
                    } catch (IllegalArgumentException e) {
                        // Category không tồn tại, nhưng không bắt buộc nên chỉ log
                        System.err.println("Warning: category not found. categoryId=" + item.getCategoryId() + ", error=" + e.getMessage());
                        // Không set category, nhưng vẫn tiếp tục
                    }
                }

                // Last-write-wins đơn giản: luôn ghi đè nếu có giá trị mới
                if (item.getType() != null) {
                    try {
                        tx.setType(TransactionType.valueOf(item.getType()));
                    } catch (IllegalArgumentException e) {
                        System.err.println("Skipping transaction sync: invalid type. type=" + item.getType() + ", error=" + e.getMessage());
                        continue; // Bỏ qua nếu type không hợp lệ
                    }
                } else if (tx.getType() == null) {
                    System.err.println("Skipping transaction sync: type is required but not provided");
                    continue; // Bỏ qua nếu không có type (required field)
                }
                
                if (item.getAmount() != null) {
                    tx.setAmount(item.getAmount());
                }
                
                if (item.getCurrency() != null) {
                    tx.setCurrency(item.getCurrency());
                } else if (tx.getCurrency() == null) {
                    System.err.println("Skipping transaction sync: currency is required but not provided");
                    continue; // Bỏ qua nếu không có currency (required field)
                }
                
                if (item.getOccurredAt() != null) {
                    tx.setOccurredAt(item.getOccurredAt());
                } else if (tx.getOccurredAt() == null) {
                    System.err.println("Skipping transaction sync: occurredAt is required but not provided");
                    continue; // Bỏ qua nếu không có occurredAt (required field)
                }
                
                if (item.getNote() != null) {
                    tx.setNote(item.getNote());
                }
                
                if (item.getTransferRefId() != null) {
                    tx.setTransferRefId(item.getTransferRefId());
                }

                tx.setDeleted(item.isDeleted());

                // ✅ Lưu transaction vào DB
                transactionRepository.save(tx);
                
                // ✅ Update wallet balance cho wallet mới
                if (tx.getWallet() != null) {
                    updateWalletBalance(tx.getWallet().getId());
                }
                
                // ✅ Nếu wallet thay đổi (update transaction), update balance cho wallet cũ
                if (oldWallet != null && tx.getWallet() != null && 
                    !oldWallet.getId().equals(tx.getWallet().getId())) {
                    updateWalletBalance(oldWallet.getId());
                }
                
            } catch (Exception e) {
                // ✅ Catch mọi exception để không rollback toàn bộ sync
                // Log lỗi nhưng tiếp tục xử lý các item khác
                System.err.println("Error syncing transaction item: " + e.getMessage());
                e.printStackTrace();
                // Tiếp tục với item tiếp theo
            }
        }

        // Sau sync trả lại danh sách hiện tại (non-deleted) cho client (tuỳ anh muốn tối ưu)
        Page<Transaction> page = transactionRepository.findByUserWithFilters(
                userId, null, null, null, null, null,
                PageRequest.of(0, Integer.MAX_VALUE)
        );

        List<TransactionResponse> responses = page.stream()
                .map(this::toResponse)
                .toList();

        // ✅ Log sau khi sync xong
        System.out.println("Transaction sync completed for userId: " + userId + 
                ", received items: " + (request.getItems() != null ? request.getItems().size() : 0) + 
                ", total transactions in DB: " + responses.size());

        return TransactionSyncResponse.builder()
                .items(responses)
                .build();
    }
}
