package com.ea.ai.rag.dms.presentation.api;

import com.ea.ai.rag.dms.application.service.AiServiceClient;
import com.ea.ai.rag.dms.domain.dto.Document;
import com.ea.ai.rag.dms.domain.vo.ai.Question;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Tag(name = "AiServiceClient", description = "API gateway for the AI Service Client")
@RestController
public class AiServiceClientRestController {
    public static final Logger logger = LoggerFactory.getLogger(AiServiceClientRestController.class);

    AiServiceClient aiServiceClient;

    public AiServiceClientRestController(AiServiceClient aiServiceClient) {
        this.aiServiceClient = aiServiceClient;
    }

    @Operation(
            summary = "Ask a relevant question (scoped RAG)",
            description = "Ask a question with optional documentId scoping; streams SSE answer + citation event before [DONE]",
            responses = {@ApiResponse(responseCode = "200", description = "ok")}
    )
    @PostMapping(value = "/v1/document/ask", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> askQuestion(
            @RequestBody @Valid Question question,
            @RequestParam(defaultValue = "2") int topK,
            @RequestParam(required = false) Double temperature,
            @RequestParam(required = false) List<Long> documentIds) {
        return aiServiceClient.streamAnswer(question, topK, temperature, documentIds);
    }

    @Operation(
            summary = "Add document to vector store",
            description = "Embeds document into the vector store, tagging chunks with documentId",
            responses = {@ApiResponse(responseCode = "200", description = "ok")}
    )
    @PostMapping(value = "/v1/document", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> addDocumentToVectorStore(@RequestBody @Valid Document document) {
        aiServiceClient.addDocumentToVectorStore(document);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(
            summary = "Delete document chunks from vector store",
            description = "Removes all chunks tagged with the given documentId",
            responses = {@ApiResponse(responseCode = "200", description = "ok")}
    )
    @DeleteMapping(value = "/v1/document/{documentId}")
    public ResponseEntity<Void> deleteDocumentChunks(@PathVariable long documentId) {
        aiServiceClient.deleteChunksByDocumentId(documentId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(
            summary = "Classify sentiment of text content",
            description = "Returns one of: Positive, Neutral, Critical, Analytical, Informative",
            responses = {@ApiResponse(responseCode = "200", description = "ok")}
    )
    @PostMapping(value = "/v1/document/classify-sentiment", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> classifySentiment(
            @RequestBody Map<String, String> body) {
        String content = body.getOrDefault("content", "");
        String sentiment = aiServiceClient.classifySentiment(content);
        return ResponseEntity.ok(Map.of("sentiment", sentiment));
    }

    @Operation(
            summary = "Embed document content (content-first, no Tika)",
            description = "Splits plain text, tags chunks with documentId, embeds in vector store (ADR-0004)",
            responses = {@ApiResponse(responseCode = "200", description = "ok")}
    )
    @PostMapping(value = "/v1/document/embed-content", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> embedContent(@RequestBody Map<String, String> body) {
        long documentId = Long.parseLong(body.get("documentId"));
        String documentName = body.getOrDefault("documentName", "");
        String content = body.getOrDefault("content", "");
        aiServiceClient.embedContent(documentId, documentName, content);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
