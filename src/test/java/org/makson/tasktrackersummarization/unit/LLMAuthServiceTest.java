package org.makson.tasktrackersummarization.unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.makson.tasktrackersummarization.dto.AccessTokenDto;
import org.makson.tasktrackersummarization.services.LLMAuthService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LLMAuthServiceTest {
    private static final AccessTokenDto VALID_TOKEN;
    private static final AccessTokenDto INVALID_TOKEN;
    private @Mock WebClient webClient;
    private @InjectMocks LLMAuthService llmAuthService;

    @Test
    @DisplayName("If the token has not expired, the cached token should be returned")
    void shouldReturnCachedToken_ifNotExpired() {
        ReflectionTestUtils.setField(llmAuthService, "currentToken", VALID_TOKEN);

        String cachedToken = llmAuthService.getToken();

        assertEquals(VALID_TOKEN.accessToken(), cachedToken);
        verify(webClient, never()).post();
    }

    static {
        VALID_TOKEN = new AccessTokenDto("accessToken", System.currentTimeMillis() + 5000);
        INVALID_TOKEN = new AccessTokenDto("accessToken", System.currentTimeMillis() - 5000);
    }
}
