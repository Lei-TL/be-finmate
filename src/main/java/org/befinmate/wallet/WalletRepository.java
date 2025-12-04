package org.befinmate.wallet;

import org.befinmate.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, String> {

    @Query("SELECT w FROM Wallet w " +
            "WHERE w.user.id = :userId AND w.deleted = false " +
            "ORDER BY w.createdAt ASC")
    List<Wallet> findByUserIdAndDeletedFalseOrderByCreatedAtAsc(@Param("userId") String userId);

    @Query("SELECT w FROM Wallet w WHERE w.id = :id AND w.user.id = :userId")
    Optional<Wallet> findByIdAndUserId(@Param("id") String id, @Param("userId") String userId);

    @Query("SELECT w FROM Wallet w WHERE w.user.id = :userId AND w.updatedAt > :since")
    List<Wallet> findByUserIdAndUpdatedAtAfter(@Param("userId") String userId,
                                               @Param("since") Instant since);
}
