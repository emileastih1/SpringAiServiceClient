package com.ea.ai.rag.dms.infrastructure.repository;

import com.ea.ai.rag.dms.domain.dto.Document;
import com.ea.ai.rag.dms.domain.vo.ai.Answer;
import com.ea.ai.rag.dms.domain.vo.ai.Question;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("ollama")
@Testcontainers
class DocumentAiRepositoryIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("pgvector/pgvector:pg16")
            .withUsername("postgres")
            .withPassword("toor")
            .withDatabaseName("doc_management_db")
            .withInitScript("test-init.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    DocumentAiRepository repository;

    @BeforeEach
    void clearVectorStore() {
        // ensure a clean slate between tests
    }

    @Test
    void contextLoads() {
        assertThat(repository).isNotNull();
    }

    @Test
    void givenDocumentIngested_whenAskMatchingQuestion_thenAnswerReturned() {
        byte[] content = "The Eiffel Tower is located in Paris, France.".getBytes();
        Document document = new Document();
        document.setDocumentName("eiffel.txt");
        document.setFile(content);

        repository.addDocumentToVectorStore(document);

        Answer answer = repository.askQuestion(new Question("Where is the Eiffel Tower located?"), 2, null);

        assertThat(answer.answer()).isNotBlank();
        assertThat(answer.answer()).isNotEqualTo("Sorry, I don't have an answer for that question");
    }

    @Test
    void givenNoDocuments_whenAskQuestion_thenFallbackAnswerReturned() {
        Answer answer = repository.askQuestion(new Question("What is the capital of Atlantis?"), 2, null);

        assertThat(answer.answer()).isEqualTo("Sorry, I don't have an answer for that question");
    }
}
