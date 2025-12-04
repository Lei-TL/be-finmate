package org.befinmate.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class CategoryResponse {

    private String id;
    private String name;
    private String type;
    private String parentId;
    private String icon;
    private boolean deleted;
    private Instant createdAt;
    private Instant updatedAt;
}
