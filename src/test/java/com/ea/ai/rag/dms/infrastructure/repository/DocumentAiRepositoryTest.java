package com.ea.ai.rag.dms.infrastructure.repository;

import com.ea.ai.rag.dms.domain.dto.Document;
import com.ea.ai.rag.dms.domain.vo.ai.Answer;
import com.ea.ai.rag.dms.domain.vo.ai.Question;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import reactor.core.publisher.Flux;

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
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
    private DocumentAiRepository documentAiRepository;

    @BeforeEach
    void setUp() {
        vectorStore = mock(VectorStore.class);
        promptTemplate = mock(PromptTemplate.class);
        chatModel = mock(ChatModel.class, Answers.RETURNS_DEEP_STUBS);
        jdbcTemplate = mock(org.springframework.jdbc.core.JdbcTemplate.class);
        documentAiRepository = new DocumentAiRepository(vectorStore, promptTemplate, chatModel, jdbcTemplate);
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

    // -------------------------------------------------------------------------
    // embedContent — content-first embedding path (no Tika, plain text, ADR-0004)
    // -------------------------------------------------------------------------

    @Test
    void embedContent_addsChunksTaggedWithDocumentIdToVectorStore() {
        ArgumentCaptor<List<org.springframework.ai.document.Document>> captor =
                ArgumentCaptor.forClass(List.class);

        documentAiRepository.embedContent(42L, "My Note", "Hello from authored content");

        verify(vectorStore).add(captor.capture());
        List<org.springframework.ai.document.Document> chunks = captor.getValue();
        assertThat(chunks).isNotEmpty();
        chunks.forEach(chunk ->
                assertThat(chunk.getMetadata().get("documentId"))
                        .as("Every chunk must carry the documentId metadata for scoped RAG")
                        .isEqualTo("42"));
    }

    @Test
    void embedContent_doesNotUseTika_splitsPlainTextDirectly() {
        // embedContent skips Tika — passing plain text must still produce chunks
        String plainText = "This is plain authored text. ".repeat(50);

        documentAiRepository.embedContent(7L, "Note", plainText);

        verify(vectorStore).add(any(List.class));
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

    @Test
    void askQuestion_streams_tokens_with_done_sentinel() {
        var doc = new org.springframework.ai.document.Document("Alex has 6 years Java experience.");
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(doc));

        Prompt prompt = mock(Prompt.class);
        when(promptTemplate.create(anyMap())).thenReturn(prompt);
        when(prompt.getInstructions()).thenReturn(List.of());

        // Mock chatModel.stream() to return Flux<ChatResponse>
        var generation = mock(Generation.class);
        var output = mock(org.springframework.ai.chat.messages.AssistantMessage.class);
        when(output.getText()).thenReturn("6 years");
        when(generation.getOutput()).thenReturn(output);
        var chatResponse = mock(ChatResponse.class);
        when(chatResponse.getResult()).thenReturn(generation);
        when(chatModel.stream(any(Prompt.class))).thenReturn(reactor.core.publisher.Flux.just(chatResponse));

        Flux<String> result = documentAiRepository.streamAnswer(new Question("How many years?"), 2, null);
        List<String> tokens = result.collectList().block();

        assertThat(tokens).contains("6 years");
        assertThat(tokens).last().isEqualTo("[DONE]");
    }

    @Test
    void addDocumentToVectorStore_splitsLargeDocumentIntoMultipleChunks() {
        String largeText = "The candidate has extensive professional experience in software engineering. "
                .repeat(400);
        Document document = new Document();
        document.setDocumentName("large-cv.txt");
        document.setFile(largeText.getBytes());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<org.springframework.ai.document.Document>> captor =
                ArgumentCaptor.forClass(List.class);

        documentAiRepository.addDocumentToVectorStore(document);

        verify(vectorStore).add(captor.capture());
        assertThat(captor.getValue())
                .as("Large document must be split into more than one chunk")
                .hasSizeGreaterThan(1);
    }

    @Test
    void addDocumentToVectorStore_chunksFitEmbeddingModelContextWindow() {
        // mxbai-embed-large has a 512-token context window (~3789 chars for this text density, ~7.4 chars/token).
        // chunkSize(400) produces ~3100-char chunks (~416 tokens); we assert ≤ 3500 chars as the safe upper bound.
        String largeText = "The candidate has extensive professional experience in software engineering. "
                .repeat(400);
        Document document = new Document();
        document.setDocumentName("large-cv.txt");
        document.setFile(largeText.getBytes());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<org.springframework.ai.document.Document>> captor =
                ArgumentCaptor.forClass(List.class);

        documentAiRepository.addDocumentToVectorStore(document);

        verify(vectorStore).add(captor.capture());
        captor.getValue().forEach(chunk ->
                assertThat(chunk.getText().length())
                        .as("Chunk must not exceed mxbai-embed-large context window")
                        .isLessThanOrEqualTo(3500));
    }

    @Test
    void askQuestion_emits_stream_error_sentinel_on_failure() {
        var doc = new org.springframework.ai.document.Document("content");
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(doc));

        Prompt prompt = mock(Prompt.class);
        when(promptTemplate.create(anyMap())).thenReturn(prompt);
        when(prompt.getInstructions()).thenReturn(List.of());

        when(chatModel.stream(any(Prompt.class))).thenReturn(
                reactor.core.publisher.Flux.error(new RuntimeException("Ollama failure")));

        Flux<String> result = documentAiRepository.streamAnswer(new Question("Q?"), 2, null);
        List<String> tokens = result.collectList().block();

        assertThat(tokens).contains("[STREAM_ERROR]");
    }
}
