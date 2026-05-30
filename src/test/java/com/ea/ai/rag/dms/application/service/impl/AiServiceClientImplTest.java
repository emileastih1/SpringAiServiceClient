package com.ea.ai.rag.dms.application.service.impl;

import com.ea.ai.rag.dms.domain.dto.Document;
import com.ea.ai.rag.dms.domain.repository.DocumentAiClientRepository;
import com.ea.ai.rag.dms.domain.vo.ai.Answer;
import com.ea.ai.rag.dms.domain.vo.ai.Question;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiServiceClientImplTest {

    @Mock
    private DocumentAiClientRepository documentAiClientRepository;

    @InjectMocks
    private AiServiceClientImpl aiServiceClientImpl;

    @Test
    void askQuestion_delegatesCallToRepository_andReturnsAnswer() {
        Question question = new Question("What is the document about?");
        Answer expectedAnswer = new Answer("The document is about testing.");
        when(documentAiClientRepository.askQuestion(question, 2, null)).thenReturn(expectedAnswer);

        Answer result = aiServiceClientImpl.askQuestion(question, 2, null);

        assertThat(result).isEqualTo(expectedAnswer);
        verify(documentAiClientRepository).askQuestion(question, 2, null);
    }

    @Test
    void addDocumentToVectorStore_delegatesCallToRepository() {
        Document document = new Document();
        document.setDocumentName("Test Document");
        document.setFile("content".getBytes());

        aiServiceClientImpl.addDocumentToVectorStore(document);

        verify(documentAiClientRepository).addDocumentToVectorStore(document);
    }
}
