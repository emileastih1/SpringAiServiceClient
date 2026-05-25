package com.ea.ai.rag.dms.infrastructure.repository.config.aiClient.ollama.rag;

import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("ollama")
@Configuration
public class OllamaRagEmbeddingClientConfig {

    @Value("${ai-client.ollama.embedding.options.model}")
    private String embeddingModelName;

    private final OllamaApi ollamaApi;

    public OllamaRagEmbeddingClientConfig(OllamaApi ollamaApi) {
        this.ollamaApi = ollamaApi;
    }

    @Bean
    @Primary
    public OllamaEmbeddingModel ollamaRagEmbeddingModel() {
        return OllamaEmbeddingModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(OllamaOptions.builder()
                        .model(embeddingModelName)
                        .build())
                .build();
    }
}
