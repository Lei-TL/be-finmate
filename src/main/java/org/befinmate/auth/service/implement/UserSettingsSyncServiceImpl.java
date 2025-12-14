package org.befinmate.auth.service.implement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.befinmate.dto.request.UserSettingsSyncRequest;
import org.befinmate.dto.response.UserSettingsSyncResponse;
import org.befinmate.entity.User;
import org.befinmate.entity.UserSettings;
import org.befinmate.auth.repository.UserRepository;
import org.befinmate.auth.repository.UserSettingsRepository;
import org.befinmate.auth.service.UserSettingsSyncService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserSettingsSyncServiceImpl implements UserSettingsSyncService {

    private final UserSettingsRepository userSettingsRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public UserSettingsSyncResponse getSettingsForUser(String userId) {

        Optional<UserSettings> optional = userSettingsRepository.findById(userId);

        if (optional.isEmpty()) {
            // Chưa có settings -> trả JSON rỗng
            return UserSettingsSyncResponse.builder()
                    .data(objectMapper.createObjectNode())
                    .updatedAt(null)
                    .build();
        }

        UserSettings settings = optional.get();

        JsonNode dataNode = parseJsonSafe(settings.getSettingsJson());

        return UserSettingsSyncResponse.builder()
                .data(dataNode)
                .updatedAt(settings.getUpdatedAt())
                .build();
    }

    @Override
    public UserSettingsSyncResponse upsertSettingsForUser(String userId, UserSettingsSyncRequest request) {

        JsonNode data = request.getData();
        if (data == null) {
            data = objectMapper.createObjectNode();
        }

        String jsonString = writeJsonSafe(data);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        UserSettings settings = userSettingsRepository.findById(userId)
                .orElseGet(() -> {
                    UserSettings newSettings = new UserSettings();
                    newSettings.setUser(user);
                    return newSettings;
                });

        settings.setSettingsJson(jsonString);

        UserSettings saved = userSettingsRepository.save(settings);

        JsonNode savedData = parseJsonSafe(saved.getSettingsJson());
        Instant updatedAt = saved.getUpdatedAt();

        return UserSettingsSyncResponse.builder()
                .data(savedData)
                .updatedAt(updatedAt)
                .build();
    }

    private JsonNode parseJsonSafe(String json) {
        if (json == null || json.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readTree(json);
        } catch (IOException e) {
            return objectMapper.createObjectNode();
        }
    }

    private String writeJsonSafe(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (IOException e) {
            throw new RuntimeException("Cannot serialize settings JSON", e);
        }
    }
}
