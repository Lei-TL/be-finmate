package org.befinmate.category.service.implement;

import lombok.RequiredArgsConstructor;
import org.befinmate.dto.request.CategoryRequest;
import org.befinmate.dto.request.CategorySyncItemRequest;
import org.befinmate.dto.request.CategorySyncRequest;
import org.befinmate.dto.response.CategoryResponse;
import org.befinmate.dto.response.CategorySyncResponse;
import org.befinmate.common.enums.TransactionType;
import org.befinmate.entity.Category;
import org.befinmate.entity.User;
import org.befinmate.category.repository.CategoryRepository;
import org.befinmate.auth.repository.UserRepository;
import org.befinmate.category.service.CategoryService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    private User getUserOrThrow(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private CategoryResponse toResponse(Category c) {
        return CategoryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .type(c.getType() != null ? c.getType().name() : null)
                .parentId(c.getParent() != null ? c.getParent().getId() : null)
                .icon(c.getIcon())
                .displayOrder(c.getDisplayOrder())
                .deleted(c.isDeleted())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    private Category getParentIfValid(String parentId) {
        if (parentId == null) return null;
        return categoryRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("Parent category not found"));
    }

    // ========= CRUD =========

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "categories")
    public List<CategoryResponse> getCategoriesForUser(String userId) {
        // ✅ Trả về tất cả categories chung cho hệ thống
        return categoryRepository.findActiveCategories()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @CacheEvict(cacheNames = "categories")
    public CategoryResponse createCategory(String userId, CategoryRequest request) {
        // ✅ Chỉ admin mới có thể tạo category (có thể thêm check role ở đây)
        Category parent = getParentIfValid(request.getParentId());

        Category category = Category.builder()
                .name(request.getName())
                .type(TransactionType.valueOf(request.getType()))
                .parent(parent)
                .icon(request.getIcon())
                .displayOrder(request.getDisplayOrder())
                .build();
        category.setDeleted(false);

        Category saved = categoryRepository.save(category);
        return toResponse(saved);
    }

    @Override
    @CacheEvict(cacheNames = "categories")
    public CategoryResponse updateCategory(String userId, String categoryId, CategoryRequest request) {
        // ✅ Chỉ admin mới có thể sửa category (có thể thêm check role ở đây)
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        if (category.isDeleted()) {
            throw new IllegalArgumentException("Category has been deleted");
        }

        Category parent = getParentIfValid(request.getParentId());

        category.setName(request.getName());
        category.setType(TransactionType.valueOf(request.getType()));
        category.setParent(parent);
        category.setIcon(request.getIcon());
        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }

        Category saved = categoryRepository.save(category);
        return toResponse(saved);
    }

    @Override
    @CacheEvict(cacheNames = "categories")
    public void deleteCategory(String userId, String categoryId) {
        // ✅ Chỉ admin mới có thể xóa category (có thể thêm check role ở đây)
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        category.setDeleted(true);
        categoryRepository.save(category);
    }

    // ========= SYNC =========

    @Override
    @Transactional(readOnly = true)
    public CategorySyncResponse syncPull(String userId, Instant since) {
        List<Category> categories;

        if (since == null) {
            // ✅ Lần đầu: gửi toàn bộ categories không deleted (chung cho hệ thống)
            categories = categoryRepository.findActiveCategories();
        } else {
            // ✅ Incremental: gửi mọi bản ghi (kể cả deleted) được cập nhật sau mốc since
            categories = categoryRepository.findByUpdatedAtAfter(since);
        }

        List<CategoryResponse> items = categories.stream()
                .sorted(Comparator.comparing(Category::getUpdatedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toResponse)
                .toList();

        return CategorySyncResponse.builder()
                .items(items)
                .build();
    }

    @Override
    @CacheEvict(cacheNames = "categories")
    public CategorySyncResponse syncPush(String userId, CategorySyncRequest request) {
        // ✅ Categories là chung cho hệ thống, không cho phép sync từ client
        // Chỉ admin mới có thể tạo/sửa/xóa categories
        // Client chỉ có thể pull categories
        
        // Trả về danh sách categories hiện tại
        List<Category> categories = categoryRepository.findActiveCategories();
        List<CategoryResponse> responses = categories.stream()
                .map(this::toResponse)
                .toList();

        return CategorySyncResponse.builder()
                .items(responses)
                .build();
    }
}
