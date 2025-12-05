package org.befinmate.category.repository;

import org.befinmate.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, String> {

    // List category: global (user is null) + của user, không deleted
    @Query("""
           SELECT c
           FROM Category c
           WHERE (c.user.id = :userId OR c.user IS NULL)
             AND c.deleted = false
           ORDER BY c.name ASC
           """)
    List<Category> findActiveByUserOrGlobal(@Param("userId") String userId);

    // Dùng cho sync: lấy mọi category (kể cả deleted) được cập nhật sau mốc since
    @Query("""
           SELECT c
           FROM Category c
           WHERE (c.user.id = :userId OR c.user IS NULL)
             AND c.updatedAt > :since
           """)
    List<Category> findByUserOrGlobalUpdatedAtAfter(@Param("userId") String userId,
                                                    @Param("since") Instant since);

    // Đọc 1 category: có thể là của user hoặc global
    @Query("""
           SELECT c
           FROM Category c
           WHERE c.id = :id
             AND (c.user.id = :userId OR c.user IS NULL)
           """)
    Optional<Category> findByIdAndUserIdOrGlobal(@Param("id") String id,
                                                 @Param("userId") String userId);

    // Sửa/xoá: chỉ cho phép category của user
    @Query("""
           SELECT c
           FROM Category c
           WHERE c.id = :id
             AND c.user.id = :userId
           """)
    Optional<Category> findByIdAndUserId(@Param("id") String id,
                                         @Param("userId") String userId);
}
