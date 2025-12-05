package org.befinmate.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FriendRequest {

    /**
     * Email của user muốn kết bạn.
     * Sau này nếu anh muốn dùng userId thì thêm field mới.
     */
    @NotBlank
    @Email
    private String targetEmail;
}
