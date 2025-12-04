package org.befinmate.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class UserSettingsSyncRequest {
    private JsonNode data;
}
