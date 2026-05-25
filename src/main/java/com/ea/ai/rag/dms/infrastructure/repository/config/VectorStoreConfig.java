package com.ea.ai.rag.dms.infrastructure.repository.config;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class VectorStoreConfig {

    private static final int MXBAI_EMBED_LARGE_DIMENSIONS = 1024;

    @Value("classpath:/ai/promptTemplate/rag-document-prompt-template.st")
    private Resource ragPromptTemplate;

    @Bean
    public VectorStore ollamaRagVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .dimensions(MXBAI_EMBED_LARGE_DIMENSIONS)
                .schemaName("vectorcontent")
                .vectorTableName("vector_store")
                .initializeSchema(false)
                .build();
    }

    @Bean
    public PromptTemplate ollamaRagPromptTemplate() {
        return new PromptTemplate(ragPromptTemplate);
    }
}
