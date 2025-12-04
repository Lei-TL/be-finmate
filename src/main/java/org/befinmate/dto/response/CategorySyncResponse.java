package org.befinmate.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CategorySyncResponse {
    private List<CategoryResponse> items;
}
