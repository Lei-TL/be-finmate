package org.befinmate.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class CategorySyncRequest {
    private List<CategorySyncItemRequest> items;
}
