package org.befinmate.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfoResponse {
    private String id;
    private String email;
    private String fullName;
    private String avatarUrl;
    private String birthday; // Format: yyyy-MM-dd hoặc null
}




