package org.befinmate.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequest {

    @NotBlank
    private String name;

    // INCOME / EXPENSE / TRANSFER
    @NotBlank
    private String type;

    // có thể null
    private String parentId;

    private String icon;
    
    private Integer displayOrder;
}
