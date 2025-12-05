package org.befinmate.dto.response;

import lombok.Builder;
import lombok.Data;
import org.befinmate.common.enums.FriendStatus;

import java.time.Instant;

@Data
@Builder
public class FriendResponse {

    // id của bản ghi Friendship
    private String id;

    // user còn lại (không phải current user)
    private String friendUserId;
    private String friendName;
    private String friendEmail;

    private FriendStatus status;
    private boolean incoming;   // true nếu đây là lời mời đến (current user là toUser)

    private Instant createdAt;
    private Instant updatedAt;
}
