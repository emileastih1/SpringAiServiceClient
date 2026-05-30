package com.ea.ai.rag.dms.infrastructure.service;

import com.ea.ai.rag.dms.domain.dto.Document;
import com.ea.ai.rag.dms.domain.vo.ai.Answer;
import com.ea.ai.rag.dms.domain.vo.ai.Question;
import com.ea.ai.rag.dms.infrastructure.repository.DocumentAiRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentDocumentAiAdapterTest {

    @Mock
    private DocumentAiRepository documentAiRepository;

    @InjectMocks
    private DocumentDocumentAiAdapter documentDocumentAiAdapter;

    @Test
    void askQuestion_delegatesCallToDocumentAiRepository_andReturnsAnswer() {
        Question question = new Question("What is the document about?");
        Answer expectedAnswer = new Answer("The document is about testing.");
        when(documentAiRepository.askQuestion(question, 2, null)).thenReturn(expectedAnswer);

        Answer result = documentDocumentAiAdapter.askQuestion(question, 2, null);

        assertThat(result).isEqualTo(expectedAnswer);
        verify(documentAiRepository).askQuestion(question, 2, null);
    }

    @Test
    void addDocumentToVectorStore_delegatesCallToDocumentAiRepository() {
        Document document = new Document();
        document.setDocumentName("Test Document");

        documentDocumentAiAdapter.addDocumentToVectorStore(document);

        verify(documentAiRepository).addDocumentToVectorStore(document);
    }
}
