package com.ea.ai.rag.dms.infrastructure.repository.config.aiClient.ollama.chat;

import org.springframework.ai.ollama.OllamaEmbeddingClient;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("ollama-chat")
@Configuration
public class OllamaChatEmbeddingClientConfig {
    @Value("${ai-client.ollama.chat.options.model}")
    private String ollamaOptionsModelName;
    private final OllamaApi ollamaApi;

    public OllamaChatEmbeddingClientConfig(OllamaApi ollamaApi) {
        this.ollamaApi = ollamaApi;
    }

    @Bean
    @Primary
    public OllamaEmbeddingClient ollamaChatEmbeddingClient() {
        return new OllamaEmbeddingClient(ollamaApi)
                .withDefaultOptions(OllamaOptions.create()
                        .withModel(ollamaOptionsModelName));
    }
}
