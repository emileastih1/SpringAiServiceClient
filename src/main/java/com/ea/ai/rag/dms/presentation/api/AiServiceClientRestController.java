package com.ea.ai.rag.dms.presentation.api;


import com.ea.ai.rag.dms.application.service.AiServiceClient;
import com.ea.ai.rag.dms.domain.dto.Document;
import com.ea.ai.rag.dms.domain.vo.ai.Answer;
import com.ea.ai.rag.dms.domain.vo.ai.Question;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AiServiceClient", description = "API gateway for the AI Service Client")
@RestController
public class AiServiceClientRestController {
    public static final Logger logger = LoggerFactory.getLogger(AiServiceClientRestController.class);

    AiServiceClient aiServiceClient;
    public AiServiceClientRestController(AiServiceClient aiServiceClient) {
        this.aiServiceClient = aiServiceClient;
    }
    @Operation(
            summary = "Ask a relevant question",
            description = "Ask a relevant question",
            responses = {
                    @ApiResponse(responseCode = "200", description = "ok", content = @Content(
                            schema = @Schema(implementation = Answer.class)
                    ))
            }
    )
    @PostMapping(value = "/v1/document/ask", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Answer> askQuestion(@RequestBody @Valid Question question) {
        return new ResponseEntity<>(aiServiceClient.askQuestion(question), HttpStatus.OK);
    }

    @Operation(
            summary = "Add document to vector store",
            description = "Add document to vector store",
            responses = {
                    @ApiResponse(responseCode = "200", description = "ok")
            }
    )
    @PostMapping(value = "/v1/document", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> addDocumentToVectorStore(@RequestBody @Valid Document document) {
        aiServiceClient.addDocumentToVectorStore(document);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
