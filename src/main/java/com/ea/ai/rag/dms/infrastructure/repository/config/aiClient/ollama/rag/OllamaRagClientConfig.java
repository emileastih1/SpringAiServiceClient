package com.ea.ai.rag.dms.infrastructure.repository.config.aiClient.ollama.rag;

import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("ollama-rag")
@Configuration
public class OllamaRagClientConfig {
    @Value("${ai-client.ollama.base-url}")
    private String ollamaBaseUrl;
    @Value("${ai-client.ollama.chat.options.model}")
    private String ollamaOptionsModelName;
    @Value("#{T(Float).parseFloat('${ai-client.ollama.chat.options.temperature}')}")
    private float ollamaOptionsTemprature;

    /**
     * We need to mark this bean as primary because the use of the pgVector dependency brings in the auto-configured OllamaChatClient
     * by default
     *
     * @return
     */
    @Primary
    @Bean
    OllamaChatClient ollamaRagChatClient() {
        return new OllamaChatClient(ollamaRagApi())
                .withDefaultOptions(OllamaOptions.create()
                        .withModel(ollamaOptionsModelName)
                        .withTemperature(ollamaOptionsTemprature));
    }

    @Bean
    public OllamaApi ollamaRagApi() {
        return new OllamaApi(ollamaBaseUrl);
    }
}
