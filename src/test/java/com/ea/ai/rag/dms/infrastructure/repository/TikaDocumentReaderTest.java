package com.ea.ai.rag.dms.infrastructure.repository;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.ByteArrayResource;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class TikaDocumentReaderTest {

    @Test
    void parsesPlainTextDocument() {
        byte[] content = "Alex Morgan Senior Software Engineer 6 years Java Spring Boot."
                .getBytes(StandardCharsets.UTF_8);
        ByteArrayResource resource = new ByteArrayResource(content, "test-cv.txt");
        TikaDocumentReader reader = new TikaDocumentReader(resource);

        List<Document> documents = assertDoesNotThrow(reader::get,
                "TikaDocumentReader.get() must not throw — verify commons-compress version is >= 1.21");

        assertThat(documents)
                .as("Should return at least one document chunk")
                .isNotEmpty();
        assertThat(documents.get(0).getText())
                .as("Parsed text should contain content from the input")
                .contains("Alex Morgan");
    }
}
