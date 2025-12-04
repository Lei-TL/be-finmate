package org.befinmate.auth;

import org.befinmate.dto.request.UserSettingsSyncRequest;
import org.befinmate.dto.response.UserSettingsSyncResponse;

public interface UserSettingsSyncService {

    UserSettingsSyncResponse getSettingsForUser(String userId);

    UserSettingsSyncResponse upsertSettingsForUser(String userId, UserSettingsSyncRequest request);
}
