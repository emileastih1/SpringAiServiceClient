package com.ea.ai.rag.dms.infrastructure.repository;

import com.ea.ai.rag.dms.domain.dto.Document;
import com.ea.ai.rag.dms.domain.vo.ai.Answer;
import com.ea.ai.rag.dms.domain.vo.ai.Question;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;


class DocumentAiRepositoryTest {

    private VectorStore vectorStore;
    private PromptTemplate promptTemplate;
    private ChatModel chatModel;
    private DocumentAiRepository documentAiRepository;

    @BeforeEach
    void setUp() {
        vectorStore = mock(VectorStore.class);
        promptTemplate = mock(PromptTemplate.class);
        chatModel = mock(ChatModel.class, Answers.RETURNS_DEEP_STUBS);
        documentAiRepository = new DocumentAiRepository(vectorStore, promptTemplate, chatModel);
    }

    @Test
    void askQuestion_whenNoSimilarDocumentsFound_returnsFallbackAnswer() {
        Question question = new Question("Unknown topic");
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

        Answer result = documentAiRepository.askQuestion(question, 2, null);

        assertThat(result.answer()).isEqualTo("Sorry, I don't have an answer for that question");
    }

    @Test
    void askQuestion_whenNullReturnedFromVectorStore_returnsFallbackAnswer() {
        Question question = new Question("Unknown topic");
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(null);

        Answer result = documentAiRepository.askQuestion(question, 2, null);

        assertThat(result.answer()).isEqualTo("Sorry, I don't have an answer for that question");
    }

    @Test
    void askQuestion_whenSimilarDocumentsFound_queriesChatModelAndReturnsAnswer() {
        Question question = new Question("What is the document about?");
        org.springframework.ai.document.Document similarDoc =
                org.springframework.ai.document.Document.builder().text("Test document content").build();
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(similarDoc));

        Prompt prompt = mock(Prompt.class);
        when(promptTemplate.create(anyMap())).thenReturn(prompt);
        when(chatModel.call(prompt).getResult().getOutput().getText()).thenReturn("The document is about testing.");

        Answer result = documentAiRepository.askQuestion(question, 2, null);

        assertThat(result.answer()).isEqualTo("The document is about testing.");
        verify(promptTemplate).create(any(Map.class));
    }

    @Test
    void addDocumentToVectorStore_processesDocumentContentAndAddsToVectorStore() {
        Document document = new Document();
        document.setDocumentName("test.txt");
        document.setFile("Hello, this is test document content for vector storage.".getBytes());

        documentAiRepository.addDocumentToVectorStore(document);

        verify(vectorStore).add(any(List.class));
    }

    @Test
    void askQuestion_searchesTopTwoSimilarDocuments() {
        Question question = new Question("What is the document about?");
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

        documentAiRepository.askQuestion(question, 2, null);

        verify(vectorStore).similaritySearch(any(SearchRequest.class));
    }

    @Test
    void askQuestion_uses_topK_from_parameter() {
        Question question = new Question("What is the document about?");
        ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);
        when(vectorStore.similaritySearch(captor.capture())).thenReturn(List.of());

        documentAiRepository.askQuestion(question, 5, null);

        assertThat(captor.getValue().getTopK()).isEqualTo(5);
    }
}
