package com.ea.ai.rag.dms.infrastructure.repository.config.aiClient.ollama.rag;

import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.openai.OpenAiEmbeddingClient;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("ollama-rag")
@Configuration
public class OllamaRagEmbeddingClientConfig {
    @Value("${ai-client.openai.api-key}")
    private String openaiApiKey;
    @Bean
    @Primary
    OpenAiEmbeddingClient openAiEmbeddingClient() {
        var openAiApi = new OpenAiApi(openaiApiKey);
        return new OpenAiEmbeddingClient(
                openAiApi,
                MetadataMode.EMBED,
                OpenAiEmbeddingOptions.builder().withModel(OpenAiApi.DEFAULT_EMBEDDING_MODEL).build(),
                RetryUtils.DEFAULT_RETRY_TEMPLATE);
    }
}
