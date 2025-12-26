package org.befinmate.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterRequest {

    @NotBlank
    private String email;

    @NotBlank
    private String password;

    private String fullName;

    private String avatarUrl;

    private String birthday; // Format: yyyy-MM-dd hoặc null
}
