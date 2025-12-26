package org.befinmate.dto.request;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String fullName;
    private String avatarUrl;
    private String birthday; // Format: yyyy-MM-dd hoặc null
    private String note; // Mô tả bản thân (có thể lưu trong UserSettings hoặc User entity)
}



