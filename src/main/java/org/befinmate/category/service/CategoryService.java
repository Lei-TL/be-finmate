package org.befinmate.category.service;

import org.befinmate.dto.request.CategoryRequest;
import org.befinmate.dto.request.CategorySyncRequest;
import org.befinmate.dto.response.CategoryResponse;
import org.befinmate.dto.response.CategorySyncResponse;

import java.time.Instant;
import java.util.List;

public interface CategoryService {

    // CRUD
    List<CategoryResponse> getCategoriesForUser(String userId);

    CategoryResponse createCategory(String userId, CategoryRequest request);

    CategoryResponse updateCategory(String userId, String categoryId, CategoryRequest request);

    void deleteCategory(String userId, String categoryId);

    // Sync (optional)
    CategorySyncResponse syncPull(String userId, Instant since);

    CategorySyncResponse syncPush(String userId, CategorySyncRequest request);
}
