package org.makson.tasktrackersummarization.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AccessTokenDto(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("expires_at") Long expiresAt
) {
}
