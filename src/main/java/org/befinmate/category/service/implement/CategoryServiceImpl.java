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
                .deleted(c.isDeleted())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    private Category getParentIfValid(String parentId, String userId) {
        if (parentId == null) return null;
        return categoryRepository.findByIdAndUserIdOrGlobal(parentId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Parent category not found or not accessible"));
    }

    // ========= CRUD =========

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "categories", key = "#userId")
    public List<CategoryResponse> getCategoriesForUser(String userId) {
        return categoryRepository.findActiveByUserOrGlobal(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @CacheEvict(cacheNames = "categories", key = "#userId")
    public CategoryResponse createCategory(String userId, CategoryRequest request) {
        User user = getUserOrThrow(userId);
        Category parent = getParentIfValid(request.getParentId(), userId);

        Category category = Category.builder()
                .user(user) // category riêng của user
                .name(request.getName())
                .type(TransactionType.valueOf(request.getType()))
                .parent(parent)
                .icon(request.getIcon())
                .build();
        category.setDeleted(false);

        Category saved = categoryRepository.save(category);
        return toResponse(saved);
    }

    @Override
    @CacheEvict(cacheNames = "categories", key = "#userId")
    public CategoryResponse updateCategory(String userId, String categoryId, CategoryRequest request) {

        // Chỉ cho phép sửa category thuộc user (không sửa global)
        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found or not owned by user"));

        if (category.isDeleted()) {
            throw new IllegalArgumentException("Category has been deleted");
        }

        Category parent = getParentIfValid(request.getParentId(), userId);

        category.setName(request.getName());
        category.setType(TransactionType.valueOf(request.getType()));
        category.setParent(parent);
        category.setIcon(request.getIcon());

        Category saved = categoryRepository.save(category);
        return toResponse(saved);
    }

    @Override
    @CacheEvict(cacheNames = "categories", key = "#userId")
    public void deleteCategory(String userId, String categoryId) {
        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found or not owned by user"));

        category.setDeleted(true);
        categoryRepository.save(category);
    }

    // ========= SYNC =========

    @Override
    @Transactional(readOnly = true)
    public CategorySyncResponse syncPull(String userId, Instant since) {
        List<Category> categories;

        if (since == null) {
            // lần đầu: gửi toàn bộ categories không deleted (global + của user)
            categories = categoryRepository.findActiveByUserOrGlobal(userId);
        } else {
            // incremental: gửi mọi bản ghi (kể cả deleted) được cập nhật sau mốc since
            categories = categoryRepository.findByUserOrGlobalUpdatedAtAfter(userId, since);
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
    @CacheEvict(cacheNames = "categories", key = "#userId")
    public CategorySyncResponse syncPush(String userId, CategorySyncRequest request) {

        User user = getUserOrThrow(userId);

        if (request.getItems() == null || request.getItems().isEmpty()) {
            return CategorySyncResponse.builder()
                    .items(List.of())
                    .build();
        }

        for (CategorySyncItemRequest item : request.getItems()) {

            // chỉ sync category của user; không ghi đè category global
            Category category = null;

            if (item.getId() != null) {
                category = categoryRepository.findByIdAndUserId(item.getId(), userId).orElse(null);
            }

            if (category == null) {
                // Kiểm tra các trường bắt buộc khi tạo mới
                if (item.getName() == null || item.getName().trim().isEmpty() || item.getType() == null) {
                    continue; // Bỏ qua nếu thiếu trường bắt buộc khi tạo mới
                }
                category = new Category();
                category.setId(item.getId() != null ? item.getId() : UUID.randomUUID().toString());
                category.setUser(user);
            }

            // Nếu muốn last-write-wins cứng hơn, có thể so sánh item.updatedAt với category.updatedAt ở đây

            if (item.getName() != null) {
                category.setName(item.getName());
            } else if (category.getName() == null) {
                continue; // Bỏ qua nếu cả item và category đều không có name
            }
            if (item.getType() != null) {
                try {
                    category.setType(TransactionType.valueOf(item.getType()));
                } catch (IllegalArgumentException e) {
                    continue; // Bỏ qua nếu type không hợp lệ
                }
            } else if (category.getType() == null) {
                continue; // Bỏ qua nếu không có type (required field)
            }
            category.setIcon(item.getIcon() != null ? item.getIcon() : category.getIcon());
            category.setDeleted(item.isDeleted());

            if (item.getParentId() != null) {
                Category parent = getParentIfValid(item.getParentId(), userId);
                category.setParent(parent);
            }

            categoryRepository.save(category);
        }

        // Sau sync có thể trả lại list active cho client
        List<Category> categories = categoryRepository.findActiveByUserOrGlobal(userId);
        List<CategoryResponse> responses = categories.stream()
                .map(this::toResponse)
                .toList();

        return CategorySyncResponse.builder()
                .items(responses)
                .build();
    }
}
