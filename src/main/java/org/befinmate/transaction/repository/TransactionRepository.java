package org.befinmate.transaction.repository;

import org.befinmate.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;

import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

    @Query("""
           SELECT t
           FROM Transaction t
           WHERE t.user.id = :userId
             AND t.deleted = false
             AND (:from IS NULL OR t.occurredAt >= :from)
             AND (:to IS NULL OR t.occurredAt <= :to)
             AND (:walletId IS NULL OR t.wallet.id = :walletId)
             AND (:categoryId IS NULL OR t.category.id = :categoryId)
             AND (:type IS NULL OR t.type = :type)
           ORDER BY t.occurredAt DESC
           """)
    Page<Transaction> findByUserWithFilters(
            @Param("userId") String userId,
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("walletId") String walletId,
            @Param("categoryId") String categoryId,
            @Param("type") String type,
            Pageable pageable
    );

    @Query("""
           SELECT t
           FROM Transaction t
           WHERE t.id = :id
             AND t.user.id = :userId
           """)
    Optional<Transaction> findByIdAndUserId(@Param("id") String id,
                                            @Param("userId") String userId);

    @Query("""
           SELECT t
           FROM Transaction t
           WHERE t.user.id = :userId
             AND t.updatedAt > :since
           """)
    List<Transaction> findByUserIdAndUpdatedAtAfter(@Param("userId") String userId,
                                                    @Param("since") Instant since);

    @Query("""
           SELECT t
           FROM Transaction t
           WHERE t.wallet.id = :walletId
             AND t.deleted = false
           """)
    List<Transaction> findByWalletIdAndDeletedFalse(@Param("walletId") String walletId);
}
