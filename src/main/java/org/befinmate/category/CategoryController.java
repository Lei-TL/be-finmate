package org.befinmate.category;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.befinmate.dto.request.CategoryRequest;
import org.befinmate.dto.request.CategorySyncRequest;
import org.befinmate.dto.response.CategoryResponse;
import org.befinmate.dto.response.CategorySyncResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    private String getUserId(Jwt jwt) {
        return jwt.getSubject();
    }

    // ======== CRUD ========

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getCategories(
            @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = getUserId(jwt);
        return ResponseEntity.ok(categoryService.getCategoriesForUser(userId));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CategoryRequest request
    ) {
        String userId = getUserId(jwt);
        CategoryResponse response = categoryService.createCategory(userId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id,
            @Valid @RequestBody CategoryRequest request
    ) {
        String userId = getUserId(jwt);
        CategoryResponse response = categoryService.updateCategory(userId, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id
    ) {
        String userId = getUserId(jwt);
        categoryService.deleteCategory(userId, id);
        return ResponseEntity.noContent().build();
    }

    // ======== SYNC (optional) ========

    @GetMapping("/sync")
    public ResponseEntity<CategorySyncResponse> syncPull(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant since
    ) {
        String userId = getUserId(jwt);
        CategorySyncResponse response = categoryService.syncPull(userId, since);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/sync")
    public ResponseEntity<CategorySyncResponse> syncPush(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CategorySyncRequest request
    ) {
        String userId = getUserId(jwt);
        CategorySyncResponse response = categoryService.syncPush(userId, request);
        return ResponseEntity.ok(response);
    }
}
