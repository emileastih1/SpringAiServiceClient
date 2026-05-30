package com.ea.ai.rag.dms.infrastructure.repository;

import com.ea.ai.rag.dms.domain.dto.Document;
import com.ea.ai.rag.dms.domain.vo.ai.Answer;
import com.ea.ai.rag.dms.domain.vo.ai.Question;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.DocumentMetadata;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public class DocumentAiRepository {

    static final String DOCUMENT_ID_META = "documentId";

    private final VectorStore vectorStore;
    private final PromptTemplate promptTemplate;
    private final ChatModel chatModel;
    private final JdbcTemplate jdbcTemplate;

    public DocumentAiRepository(VectorStore vectorStore, PromptTemplate promptTemplate,
                                 ChatModel chatModel, JdbcTemplate jdbcTemplate) {
        this.vectorStore = vectorStore;
        this.promptTemplate = promptTemplate;
        this.chatModel = chatModel;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addDocumentToVectorStore(Document document) {
        ByteArrayResource byteArrayResource = new ByteArrayResource(document.getFile(), document.getDocumentName());
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(byteArrayResource);
        List<org.springframework.ai.document.Document> documents = tikaDocumentReader.get();

        TextSplitter textSplitter = TokenTextSplitter.builder()
                .withChunkSize(400)
                .withMinChunkSizeChars(50)
                .withMaxNumChunks(1000)
                .withKeepSeparator(true)
                .build();
        List<org.springframework.ai.document.Document> splitDocuments = textSplitter.apply(documents);

        // Tag each chunk with the ICM documentId for scoped retrieval (ADR-0007)
        if (document.getDocumentId() != null) {
            String docIdStr = String.valueOf(document.getDocumentId());
            splitDocuments.forEach(chunk ->
                    chunk.getMetadata().put(DOCUMENT_ID_META, docIdStr));
        }

        vectorStore.add(splitDocuments);
    }

    // Delete all chunks belonging to a documentId (ADR-0007 re-embed on edit)
    public void deleteChunksByDocumentId(long documentId) {
        jdbcTemplate.update(
                "DELETE FROM vectorcontent.vector_store WHERE metadata->>'documentId' = ?",
                String.valueOf(documentId));
    }

    public Answer askQuestion(Question question, int topK, Double temperature) {
        List<org.springframework.ai.document.Document> similarDocuments = vectorStore.similaritySearch(
                SearchRequest.builder().query(question.question()).topK(topK).build());

        if (similarDocuments != null && !similarDocuments.isEmpty()) {
            List<String> contentList = similarDocuments.stream()
                    .map(org.springframework.ai.document.Document::getText)
                    .toList();
            Map<String, Object> promptParameters = new HashMap<>();
            promptParameters.put("input", question.question());
            promptParameters.put("documents", String.join("\n", contentList));
            Prompt prompt = promptTemplate.create(promptParameters);
            if (temperature != null) {
                prompt = new Prompt(prompt.getInstructions(),
                        OllamaChatOptions.builder().temperature(temperature).build());
            }
            ChatResponse response = chatModel.call(prompt);
            return new Answer(response.getResult().getOutput().getText());
        } else {
            return new Answer("Sorry, I don't have an answer for that question");
        }
    }

    // Classify sentiment of text content via LLM (ADR-0007)
    public String classifySentiment(String content) {
        String sentimentPrompt = "Classify the sentiment of the following text into exactly ONE of these labels: "
                + "Positive, Neutral, Critical, Analytical, Informative. "
                + "Respond with only the label, nothing else.\n\nText:\n" + content;
        Prompt prompt = new Prompt(sentimentPrompt,
                OllamaChatOptions.builder().temperature(0.0).build());
        ChatResponse response = chatModel.call(prompt);
        String raw = response.getResult().getOutput().getText().trim();
        // Normalise: extract first word that matches a known label
        for (String label : List.of("Positive", "Neutral", "Critical", "Analytical", "Informative")) {
            if (raw.contains(label)) return label;
        }
        return "Neutral";
    }

    public Flux<String> streamAnswer(Question question, int topK, Double temperature,
                                     List<Long> documentIds) {
        SearchRequest.Builder searchBuilder = SearchRequest.builder()
                .query(question.question())
                .topK(topK);

        // Apply documentId filter when specified (ADR-0007 scoped RAG)
        if (documentIds != null && !documentIds.isEmpty()) {
            FilterExpressionBuilder b = new FilterExpressionBuilder();
            if (documentIds.size() == 1) {
                searchBuilder.filterExpression(
                        b.eq(DOCUMENT_ID_META, String.valueOf(documentIds.get(0))).build());
            } else {
                List<String> strIds = documentIds.stream().map(String::valueOf).toList();
                searchBuilder.filterExpression(
                        b.in(DOCUMENT_ID_META, strIds.toArray()).build());
            }
        }

        List<org.springframework.ai.document.Document> similarDocuments =
                vectorStore.similaritySearch(searchBuilder.build());

        if (similarDocuments == null || similarDocuments.isEmpty()) {
            return Flux.just("Sorry, I don't have an answer for that question", "[DONE]");
        }

        // Collect referenced documentIds for citations
        Set<String> referencedIds = new LinkedHashSet<>();
        List<String> contentList = similarDocuments.stream()
                .map(chunk -> {
                    Object docId = chunk.getMetadata().get(DOCUMENT_ID_META);
                    if (docId != null) referencedIds.add(docId.toString());
                    return chunk.getText();
                })
                .toList();

        Map<String, Object> promptParameters = new HashMap<>();
        promptParameters.put("input", question.question());
        promptParameters.put("documents", String.join("\n", contentList));
        Prompt prompt = promptTemplate.create(promptParameters);

        Flux<ChatResponse> responseFlux;
        if (temperature != null) {
            responseFlux = chatModel.stream(new Prompt(prompt.getInstructions(),
                    OllamaChatOptions.builder().temperature(temperature).build()));
        } else {
            responseFlux = chatModel.stream(prompt);
        }

        // Stream answer tokens, then emit a citations metadata event before [DONE]
        String citationsEvent = referencedIds.isEmpty() ? null
                : "[CITATIONS:" + String.join(",", referencedIds) + "]";

        Flux<String> answerFlux = responseFlux
                .map(r -> r.getResult().getOutput().getText())
                .filter(text -> text != null && !text.isEmpty());

        if (citationsEvent != null) {
            answerFlux = answerFlux.concatWith(Flux.just(citationsEvent));
        }

        return answerFlux
                .concatWith(Flux.just("[DONE]"))
                .onErrorResume(e -> Flux.just("[STREAM_ERROR]"));
    }

    // Backward-compatible overload — no documentId scoping
    public Flux<String> streamAnswer(Question question, int topK, Double temperature) {
        return streamAnswer(question, topK, temperature, null);
    }
}
