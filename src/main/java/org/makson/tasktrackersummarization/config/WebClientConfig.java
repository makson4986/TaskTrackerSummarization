package org.makson.tasktrackersummarization.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient apiWebClient(@Value("${llm.api-url}") String url) {
        return WebClient.builder()
                .baseUrl(url)
                .build();
    }

    @Bean
    public WebClient authWebClient(@Value("${llm.auth-url}") String url) {
        return WebClient.builder()
                .baseUrl(url)
                .build();
    }
}
