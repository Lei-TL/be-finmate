package org.befinmate.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UserSettingsSyncResponse {
    private JsonNode data;
    private Instant updatedAt;
}
