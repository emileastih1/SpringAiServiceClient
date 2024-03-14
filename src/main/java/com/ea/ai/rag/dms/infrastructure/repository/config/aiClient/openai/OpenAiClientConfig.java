package com.ea.ai.rag.dms.infrastructure.repository.config.aiClient.openai;

import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("openai")
@Configuration
public class OpenAiClientConfig {
    @Value("${ai-client.openai.api-key}")
    private String openAiApiKey;
    @Value("${ai-client.openai.chat.model}")
    private String openAiModelName;
    @Value("#{T(Float).parseFloat('${ai-client.openai.chat.temperature}')}")
    private float openAiTemperature;
    @Value("${ai-client.openai.chat.max-tokens}")
    private int openAiMaxTokens;

    @Bean
    public OpenAiApi openAiChatApi() {
        return new OpenAiApi(openAiApiKey);
    }

    @Bean
    OpenAiChatClient openAiChatClient(OpenAiApi openAiApi) {
        return new OpenAiChatClient(openAiApi,
                OpenAiChatOptions.builder()
                        .withModel(openAiModelName)
                        .withTemperature(openAiTemperature)
                        .withMaxTokens(openAiMaxTokens)
                        .build())
                ;
    }
}
