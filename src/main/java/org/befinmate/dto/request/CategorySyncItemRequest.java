package org.befinmate.dto.request;

import lombok.Data;

import java.time.Instant;

@Data
public class CategorySyncItemRequest {

    private String id;          // client có thể tự generate or null

    private Instant updatedAt;  // timestamp local client (chưa dùng nhiều, để dành last-write-wins)

    private boolean deleted;

    private String name;
    private String type;        // INCOME / EXPENSE / TRANSFER
    private String parentId;
    private String icon;
}
