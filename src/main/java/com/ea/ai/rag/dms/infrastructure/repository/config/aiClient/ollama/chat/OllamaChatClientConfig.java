package com.ea.ai.rag.dms.infrastructure.repository.config.aiClient.ollama.chat;

import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("ollama-chat")
@Configuration
public class OllamaChatClientConfig {

    @Value("${ai-client.ollama.base-url}")
    private String ollamaBaseUrl;
    @Value("${ai-client.ollama.chat.options.model}")
    private String ollamaOptionsModelName;
    @Value("#{T(Double).parseDouble('${ai-client.ollama.chat.options.temperature}')}")
    private double ollamaOptionsTemperature;

    @Primary
    @Bean
    OllamaChatModel chatModel() {
        return OllamaChatModel.builder()
                .ollamaApi(ollamaChatApi())
                .defaultOptions(OllamaChatOptions.builder()
                        .model(ollamaOptionsModelName)
                        .temperature(ollamaOptionsTemperature)
                        .build())
                .build();
    }

    @Bean
    public OllamaApi ollamaChatApi() {
        return OllamaApi.builder()
                .baseUrl(ollamaBaseUrl)
                .build();
    }
}
