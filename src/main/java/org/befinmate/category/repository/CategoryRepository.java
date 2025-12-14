package org.befinmate.category.repository;

import org.befinmate.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, String> {

    // ✅ List category: tất cả categories chung cho hệ thống, không deleted
    @Query("""
           SELECT c
           FROM Category c
           WHERE c.deleted = false
           ORDER BY c.displayOrder ASC, c.name ASC
           """)
    List<Category> findActiveCategories();

    // ✅ Lấy categories theo type
    @Query("""
           SELECT c
           FROM Category c
           WHERE c.type = :type
             AND c.deleted = false
           ORDER BY c.displayOrder ASC, c.name ASC
           """)
    List<Category> findByType(@Param("type") org.befinmate.common.enums.TransactionType type);

    // ✅ Dùng cho sync: lấy mọi category (kể cả deleted) được cập nhật sau mốc since
    @Query("""
           SELECT c
           FROM Category c
           WHERE c.updatedAt > :since
           """)
    List<Category> findByUpdatedAtAfter(@Param("since") Instant since);
}
